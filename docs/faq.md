# Preguntas frecuentes

??? question "¿Por qué dos ejecuciones dan resultados distintos?"
    El GRASP es aleatorio. Use `--seed` para fijar la semilla y obtener siempre la misma salida.

??? question "¿Qué significan los depósitos 101, 102…?"
    Son copias virtuales del depósito 0 (el hotel). Cada ruta adicional necesita su propio
    nodo de inicio y fin en la lista doblemente enlazada, así que se numeran a partir de
    POI + 1. Al consultar coordenadas, horarios o distancias se tratan como el nodo 0.

??? question "¿Por qué algunos POI no se visitan nunca?"
    Si no pueden incluirse en ninguna ruta sin incumplir su ventana temporal o la duración
    máxima, el algoritmo no los inserta. En el TOPTW no es obligatorio visitar todos los POI.

??? question "Obtengo «Formato de instancia no válido»"
    El fichero tiene menos líneas de nodo que las indicadas en la primera línea o algún campo
    no es numérico. Revise el [formato](formato-instancias.md).

??? question "¿Cambian los resultados respecto a la versión original?"
    En las instancias de Solomon (`c*`, `r*`, `rc*`), no: con la misma semilla las rutas son
    idénticas (lo comprueba un test de regresión). En las `pr*` sí, porque se corrigió la
    lectura de las ventanas temporales.

??? question "¿Cómo cambio la estrategia de selección de la RCL?"
    Desde código, con `setStrategy(SelectionStrategy.RANDOM | FUZZY_BEST | FUZZY_ALPHA_CUT)`
    y `setAlpha(...)`. Por defecto se usa `FUZZY_ALPHA_CUT` con α = 0,8, igual que el original.
