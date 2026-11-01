# ExpositoTOP — Referencia del código

**RTVRPTW: Recomendador Turístico con Ventanas de Tiempo.**

El sistema construye rutas turísticas de varios días: cada día es una ruta que sale del
hotel (depósito), visita puntos de interés (POI) dentro de su horario de apertura y vuelve
antes de una hora límite. El objetivo es maximizar el interés total (puntuación) de los POI
visitados. Formalmente es el *Team Orienteering Problem with Time Windows* (TOPTW), que se
resuelve con la metaheurística **GRASP**.

## Componentes

| Paquete | Clase | Responsabilidad |
|---|---|---|
| `top` | top::TOPTW | Modelo de la instancia: POI, ventanas temporales y distancias |
| `top` | top::TOPTWReader | Lectura de instancias en formato Cordeau/Righini |
| `top` | top::TOPTWSolution | Rutas como listas doblemente enlazadas; fitness |
| `top` | top::TOPTWEvaluator | Función objetivo |
| `top` | top::TOPTWGRASP | Construcción voraz aleatorizada con RCL y selección borrosa |
| `top` | top::MainTOPTW | Programa de línea de órdenes |
| `es.ull.esit.utilities` | es::ull::esit::utilities::ExpositoUtilities | Formato de tablas, ficheros, grafos |
| `es.ull.esit.utilities` | es::ull::esit::utilities::PowerSet, es::ull::esit::utilities::BellmanFord | Algoritmos auxiliares |
| `es.ull.esit.utils` | es::ull::esit::utils::Pair | Par inmutable |

## Flujo de ejecución

1. top::MainTOPTW lee cada instancia con top::TOPTWReader::readProblem.
2. Crea una top::TOPTWSolution y un top::TOPTWGRASP.
3. top::TOPTWGRASP::grasp repite top::TOPTWGRASP::computeGreedySolution e informa de la
   media y de la mejor puntuación.

El portal completo (manual de uso, arquitectura, calidad y decisiones de diseño) se genera con MkDocs.