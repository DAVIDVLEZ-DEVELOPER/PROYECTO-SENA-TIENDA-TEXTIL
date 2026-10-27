#!/usr/bin/env bash
# =============================================================================
# SIMULADOR DE CARGA Y ESTRES CONCURRENTE EN BASH (CURL / LINUX / MAC)
# =============================================================================
set -e

BASE_URL="${1:-http://localhost:8081}"
PETICIONES="${2:-100}"
CONCURRENCIA="${3:-10}"

echo "================================================================="
echo "   SIMULADOR DE CARGA Y ESTRES CONCURRENTE (BASH / CURL)        "
echo "================================================================="
echo "Servidor Destino:   $BASE_URL"
echo "Total Peticiones:   $PETICIONES"
echo "Concurrencia:       $CONCURRENCIA hilos"
echo "================================================================="

ENDPOINTS=(
  "/health/live"
  "/api/productos"
  "/api/carrusel"
  "/api/secciones-inicio"
  "/api/pagos/config"
)

EXITOS=0
FALLOS=0
TIEMPO_INICIO=$(date +%s)

for ((i=1; i<=PETICIONES; i++)); do
  EP_IDX=$((i % ${#ENDPOINTS[@]}))
  EP="${ENDPOINTS[$EP_IDX]}"
  
  (
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "X-Correlation-ID: bash-load-$i" "$BASE_URL$EP" || echo "000")
    if [ "$HTTP_CODE" -ge 200 ] && [ "$HTTP_CODE" -lt 300 ]; then
      echo "OK" >> /tmp/carga_resultados.txt
    else
      echo "FAIL" >> /tmp/carga_resultados.txt
    fi
  ) &

  # Limitar concurrencia activa
  if (( i % CONCURRENCIA == 0 )); then
    wait
  fi
done

wait
TIEMPO_FIN=$(date +%s)
DURACION=$((TIEMPO_FIN - TIEMPO_INICIO))
[ "$DURACION" -eq 0 ] && DURACION=1

EXITOS=$(grep -c "OK" /tmp/carga_resultados.txt 2>/dev/null || echo 0)
FALLOS=$(grep -c "FAIL" /tmp/carga_resultados.txt 2>/dev/null || echo 0)
rm -f /tmp/carga_resultados.txt

THROUGHPUT=$((PETICIONES / DURACION))

echo ""
echo "================================================================="
echo "                    METRICAS Y RESULTADOS                       "
echo "================================================================="
echo "Tiempo Total:       ${DURACION}s"
echo "Peticiones Exitosas: $EXITOS"
echo "Peticiones Fallidas: $FALLOS"
echo "Throughput Aprox:   $THROUGHPUT req/s"
echo "================================================================="
