#!/usr/bin/env python3
"""Genera un PDF con todas las páginas del portal MkDocs, en el orden de la navegación.

Uso:
    mkdocs build
    python scripts/build_pdf.py [--site site] [--config mkdocs.yml] [--out docs/pdf/ExpositoTOP-documentacion.pdf]

Sirve el directorio del sitio en un puerto local, abre cada página con Chromium (Playwright),
espera a que se dibujen los diagramas Mermaid, la imprime a PDF ocultando la navegación y une
todas las páginas con una portada y un índice con marcadores.
"""
import argparse
import functools
import http.server
import io
import re
import threading
from datetime import date
from pathlib import Path

import yaml
from playwright.sync_api import sync_playwright
from pypdf import PdfReader, PdfWriter


class _Loader(yaml.SafeLoader):
    """Cargador YAML que ignora las etiquetas !!python/name de mkdocs.yml."""


_Loader.add_multi_constructor("tag:yaml.org,2002:python/", lambda loader, suffix, node: None)
_Loader.add_multi_constructor("!", lambda loader, suffix, node: None)

PRINT_CSS = """
.md-header, .md-tabs, .md-sidebar, .md-footer, .md-source, .md-content__button,
.md-top, .headerlink, .md-search { display: none !important; }
.md-main__inner { margin: 0 !important; }
.md-content { max-width: none !important; margin: 0 !important; }
.md-content__inner { margin: 0 !important; padding: 0 !important; }
.md-typeset { font-size: 10.5pt; }
.md-typeset pre, .md-typeset table { page-break-inside: avoid; }
.md-typeset details { display: block; }
.md-typeset details > summary { pointer-events: none; }
"""


def nav_pages(nav, prefix=()):
    """Aplana la navegación de mkdocs.yml en una lista de (título, ruta .md)."""
    for item in nav:
        if isinstance(item, str):
            yield (" / ".join(prefix) or item, item)
        elif isinstance(item, dict):
            for title, value in item.items():
                if isinstance(value, str):
                    yield (title, value)
                else:
                    yield from nav_pages(value, prefix + (title,))


def md_to_url(path):
    """Convierte la ruta de una página Markdown en su URL dentro del sitio."""
    if re.search(r"(^|/)index\.md$", path):
        return path[: -len("index.md")]
    return path[: -len(".md")] + "/"


class _QuietHandler(http.server.SimpleHTTPRequestHandler):
    """Servidor de ficheros estáticos sin registro de peticiones."""

    def log_message(self, *args):
        pass


def serve(directory):
    handler = functools.partial(_QuietHandler, directory=str(directory))
    server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), handler)
    threading.Thread(target=server.serve_forever, daemon=True).start()
    return server


def cover_html(title, subtitle, entries):
    rows = "".join(f"<li>{t}</li>" for t, _ in entries)
    return f"""<!doctype html><html lang="es"><meta charset="utf-8"><style>
    body{{font-family:'Helvetica Neue',Arial,sans-serif;color:#1a1f36;margin:0}}
    .cover{{height:250mm;display:flex;flex-direction:column;justify-content:center;padding:0 20mm;
      border-left:8mm solid #3f51b5;page-break-after:always}}
    h1{{font-size:34pt;margin:0 0 6mm}} p{{font-size:14pt;color:#4a5170;margin:2mm 0}}
    .toc{{padding:0 20mm}} .toc h2{{font-size:18pt;border-bottom:1px solid #ccd}}
    .toc ol{{font-size:11pt;line-height:1.7}}</style>
    <div class="cover"><h1>{title}</h1><p>{subtitle}</p>
    <p>Documentación técnica generada con MkDocs + Doxygen</p><p>{date.today():%d/%m/%Y}</p></div>
    <div class="toc"><h2>Contenido</h2><ol>{rows}</ol></div></html>"""


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--site", default="site")
    parser.add_argument("--config", default="mkdocs.yml")
    parser.add_argument("--out", default="docs/pdf/ExpositoTOP-documentacion.pdf")
    args = parser.parse_args()

    config = yaml.load(Path(args.config).read_text(encoding="utf-8"), Loader=_Loader)
    site = Path(args.site)
    entries = [(t, p) for t, p in nav_pages(config["nav"]) if (site / md_to_url(p) / "index.html").exists()
               or (site / md_to_url(p)).with_suffix(".html").exists()]

    server = serve(site)
    base = f"http://127.0.0.1:{server.server_address[1]}/"
    writer = PdfWriter()
    pdf_opts = dict(format="A4", print_background=True,
                    margin={"top": "16mm", "bottom": "16mm", "left": "14mm", "right": "14mm"},
                    display_header_footer=True, header_template="<span></span>",
                    footer_template='<div style="font-size:8pt;width:100%;text-align:center;color:#888">'
                                    '<span class="title"></span> · <span class="pageNumber"></span></div>')
    with sync_playwright() as pw:
        browser = pw.chromium.launch()
        page = browser.new_page()
        page.set_content(cover_html(config["site_name"], config.get("site_description", ""), entries))
        writer.append(PdfReader(io.BytesIO(page.pdf(format="A4", print_background=True))))
        for title, md in entries:
            page.goto(base + md_to_url(md), wait_until="networkidle")
            page.add_style_tag(content=PRINT_CSS)
            page.evaluate("document.querySelectorAll('details').forEach(d => d.open = true)")
            try:
                page.wait_for_function(
                    "[...document.querySelectorAll('.mermaid')].every(e => e.querySelector('svg'))",
                    timeout=8000)
            except Exception:  # sin Mermaid o sin conexión al CDN: se imprime igualmente
                pass
            start = len(writer.pages)
            writer.append(PdfReader(io.BytesIO(page.pdf(**pdf_opts))))
            writer.add_outline_item(title, start)
            print(f"  {title}: {len(writer.pages) - start} pág.")
        browser.close()
    server.shutdown()

    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    writer.add_metadata({"/Title": config["site_name"], "/Author": config.get("site_author", "")})
    with out.open("wb") as fh:
        writer.write(fh)
    print(f"PDF generado: {out} ({len(writer.pages)} páginas)")


if __name__ == "__main__":
    main()
