#!/usr/bin/env bash
# ==============================================================================
# Script de Verificacion Pre-Vuelo (Preflight Health Check) - D&D Textil
# ==============================================================================

set -uo pipefail

BACKEND_URL="${1:-http://localhost:8081}"
FRONTEND_URL="${2:-http://localhost:3001}"
HOST_DB="${3:-localhost}"
PUERTO_DB="${4:-5432}"

echo "================================================================================"
echo "VERIFICACION PRE-VUELO DEL SISTEMA - D&D TEXTIL"
echo "Fecha y Hora: $(date '+%Y-%m-%d %H:%M:%S')"
echo "================================================================================"

TOTAL_CHECKS=0
CHECKS_EXITOSOS=0

evaluar_check() {
    local nombre="$1"
    local condicion="$2"
    local detalle="$3"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    if [ "$condicion" -eq 0 ]; then
        CHECKS_EXITOSOS=$((CHECKS_EXITOSOS + 1))
        echo "  [OK] ${nombre}: ${detalle}"
    else
        echo "  [ALERTA] ${nombre}: ${detalle}"
    fi
}

echo -e "\n[1. Configuracion y Variables de Entorno]"
if [ -f ".env" ]; then
    evaluar_check "Archivo .env" 0 "Presente en la raiz"
else
    evaluar_check "Archivo .env" 1 "No encontrado. Ejecutar: cp .env.example .env"
fi

echo -e "\n[2. Disponibilidad de Puertos y Servicios]"
nc -z "${HOST_DB}" "${PUERTO_DB}" 2>/dev/null
evaluar_check "Puerto PostgreSQL (${PUERTO_DB})" $? "Conexion TCP verificada"

nc -z localhost 8081 2>/dev/null
BACKEND_TCP=$?
evaluar_check "Puerto Backend Java (8081)" $BACKEND_TCP "Conexion TCP verificada"

nc -z localhost 3001 2>/dev/null
evaluar_check "Puerto Frontend Web (3001)" $? "Conexion TCP verificada"

echo -e "\n[3. Probes de Observabilidad HTTP]"
if [ $BACKEND_TCP -eq 0 ]; then
    curl -sf "${BACKEND_URL}/health/live" >/dev/null 2>&1
    evaluar_check "Liveness Probe (/health/live)" $? "Servidor vivo y respondiendo"

    curl -sf "${BACKEND_URL}/health/ready" >/dev/null 2>&1
    evaluar_check "Readiness Probe (/health/ready)" $? "Base de datos y dependencias listas"

    curl -sf "${BACKEND_URL}/api/pagos/config" >/dev/null 2>&1
    evaluar_check "Configuracion Pasarela Wompi" $? "Endpoint de pagos activo"
fi

echo -e "\n[4. Credenciales de Prueba (ISO/IEC 25010)]"
echo "  - Administrador: admin@ddtextil.com     / admin123     (Panel /admin)"
echo "  - Vendedor:      vendedor@ddtextil.com  / vendedor123  (Panel /vendedor)"
echo "  - Cliente Demo:  cliente@ddtextil.com   / cliente123   (Panel /cliente)"
echo "  - Cliente Alt:   cliente@gmail.com      / cliente123   (Panel /cliente)"

echo -e "\n================================================================================"
echo "RESUMEN DE VERIFICACION PRE-VUELO"
echo "Comprobaciones Evaluadas: ${TOTAL_CHECKS}"
echo "Comprobaciones Exitosas: ${CHECKS_EXITOSOS}"
if [ "${CHECKS_EXITOSOS}" -eq "${TOTAL_CHECKS}" ] && [ "${TOTAL_CHECKS}" -gt 0 ]; then
    echo "DICTAMEN: ENTORNO COMPLETAMENTE OPERATIVO PARA SUSTENTACION Y PRODUCCION"
else
    echo "DICTAMEN: VERIFICAR SERVICIOS INACTIVOS MEDIANTE 'docker compose up -d'"
fi
echo "================================================================================"
