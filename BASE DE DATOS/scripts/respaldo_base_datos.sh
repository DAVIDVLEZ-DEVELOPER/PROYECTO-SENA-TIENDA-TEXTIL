#!/usr/bin/env bash
# ==============================================================================
# Script de Respaldo y Rotacion de Base de Datos PostgreSQL - D&D Textil
# Estandares: DRP, ISO/IEC 25010 (Fiabilidad y Recuperabilidad)
# ==============================================================================

set -euo pipefail

HOST_DB="${1:-localhost}"
PUERTO_DB="${2:-5432}"
NOMBRE_DB="${3:-tienda_digital_textiles_db}"
USUARIO_DB="${4:-postgres}"
DIAS_RETENCION="${5:-7}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DIR_SALIDA="${SCRIPT_DIR}/../respaldos"
mkdir -p "${DIR_SALIDA}"

MARCA_TIEMPO="$(date +%Y%m%d_%H%M%S)"
ARCHIVO_SQL="${DIR_SALIDA}/respaldo_ddtextil_${MARCA_TIEMPO}.sql"
ARCHIVO_GZ="${ARCHIVO_SQL}.gz"
ARCHIVO_SHA="${ARCHIVO_GZ}.sha256"

echo "================================================================================"
echo "EJECUCION DE RESPALDO DE BASE DE DATOS - D&D TEXTIL"
echo "Fecha y Hora: $(date '+%Y-%m-%d %H:%M:%S')"
echo "Base de Datos: ${NOMBRE_DB} en ${HOST_DB}:${PUERTO_DB} (Usuario: ${USUARIO_DB})"
echo "Destino: ${DIR_SALIDA}"
echo "================================================================================"

if command -v pg_dump >/dev/null 2>&1; then
    echo "1. Ejecutando pg_dump nativo..."
    pg_dump -h "${HOST_DB}" -p "${PUERTO_DB}" -U "${USUARIO_DB}" -d "${NOMBRE_DB}" \
        --no-owner --no-privileges -f "${ARCHIVO_SQL}"
elif command -v docker >/dev/null 2>&1; then
    echo "1. pg_dump local no encontrado. Ejecutando en contenedor Docker 'ddtextil-db'..."
    docker exec ddtextil-db pg_dump -U "${USUARIO_DB}" -d "${NOMBRE_DB}" --no-owner --no-privileges > "${ARCHIVO_SQL}"
else
    echo "Error: No se encontro pg_dump ni Docker en el sistema." >&2
    exit 1
fi

echo "2. Comprimiendo volcado con gzip..."
gzip -9 -f "${ARCHIVO_SQL}"

echo "3. Generando suma de verificacion SHA-256..."
if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "${ARCHIVO_GZ}" > "${ARCHIVO_SHA}"
elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "${ARCHIVO_GZ}" > "${ARCHIVO_SHA}"
fi

echo "4. Aplicando politica de rotacion (${DIAS_RETENCION} dias)..."
find "${DIR_SALIDA}" -type f -name "respaldo_ddtextil_*.sql.gz*" -mtime +"${DIAS_RETENCION}" -delete

echo "================================================================================"
echo "RESPALDO COMPLETADO EXITOSAMENTE"
echo "Archivo: ${ARCHIVO_GZ}"
echo "================================================================================"
