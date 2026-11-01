# ADR 0006 — Documentación Docs-as-Code con Doxygen y MkDocs

**Estado:** aceptada

## Contexto

La práctica exige documentar el código con Doxygen (con grafos de Graphviz) y publicar un
portal técnico unificado. Había que decidir cómo unir la referencia generada con las guías
escritas a mano.

## Opciones

1. HTML de Doxygen con el tema *Doxygen Awesome* y las guías como páginas de Doxygen.
2. MkDocs Material con las guías en Markdown y la referencia integrada con **MkDoxy**.

## Decisión

Opción 2. MkDocs Material da mejor navegación, búsqueda y diseño para las guías; MkDoxy
convierte la salida XML de Doxygen en páginas del mismo portal. El HTML completo de Doxygen
(con los grafos interactivos) se publica también en `/doxygen/` y el PDF se obtiene con LaTeX
(`refman.pdf`) y con un *script* que imprime el portal.

## Consecuencias

- Un único portal con guías, decisiones y referencia del código.
- La construcción requiere Doxygen, Graphviz y Python; se automatiza en GitHub Actions.
- El *script* de PDF usa Playwright (Chromium) en lugar de `mkdocs-with-pdf`, que no se
  mantiene desde 2021 y no funciona con las versiones actuales de WeasyPrint.
