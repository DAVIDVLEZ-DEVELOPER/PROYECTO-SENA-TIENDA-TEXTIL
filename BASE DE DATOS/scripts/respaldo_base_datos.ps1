<#
.SYNOPSIS
    Script automatizado de respaldo y rotacion de base de datos PostgreSQL (DRP).
.DESCRIPTION
    Ejecuta un volcado consistente (pg_dump) de la base de datos de D&D Textil,
    genera un archivo de verificacion de integridad SHA-256 y aplica una
    politica de rotacion de 7 dias para cumplir con el RPO <= 15 min y RTO <= 2 horas.
#>

param(
    [string]$HostDb = "localhost",
    [int]$PuertoDb = 5432,
    [string]$NombreDb = "tienda_digital_textiles_db",
    [string]$UsuarioDb = "postgres",
    [string]$DirectorioSalida = "",
    [int]$DiasRetencion = 7
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
if ([string]::IsNullOrWhiteSpace($DirectorioSalida)) {
    $DirectorioSalida = Join-Path (Split-Path -Parent $scriptDir) "respaldos"
}

if (-not (Test-Path $DirectorioSalida)) {
    New-Item -ItemType Directory -Path $DirectorioSalida -Force | Out-Null
}

$marcaTiempo = Get-Date -Format "yyyyMMdd_HHmmss"
$nombreArchivo = "respaldo_ddtextil_${marcaTiempo}.sql"
$rutaArchivoSql = Join-Path $DirectorioSalida $nombreArchivo
$rutaArchivoGz = "${rutaArchivoSql}.gz"
$rutaArchivoChecksum = "${rutaArchivoGz}.sha256"

Write-Output "================================================================================"
Write-Output "EJECUCION DE RESPALDO DE BASE DE DATOS - D&D TEXTIL"
Write-Output "Fecha y Hora: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output "Base de Datos: $NombreDb en $HostDb:$PuertoDb (Usuario: $UsuarioDb)"
Write-Output "Destino: $DirectorioSalida"
Write-Output "================================================================================"

# Verificacion de disponibilidad de pg_dump o ejecucion via Docker
$comandoPgDump = Get-Command "pg_dump" -ErrorAction SilentlyContinue

if ($comandoPgDump) {
    Write-Output "1. Ejecutando pg_dump nativo..."
    & pg_dump -h $HostDb -p $PuertoDb -U $UsuarioDb -d $NombreDb -F p --no-owner --no-privileges -f $rutaArchivoSql
} else {
    Write-Output "1. pg_dump no encontrado en PATH local. Intentando ejecucion mediante contenedor Docker 'ddtextil-db'..."
    $comandoDocker = Get-Command "docker" -ErrorAction SilentlyContinue
    if ($comandoDocker) {
        & docker exec ddtextil-db pg_dump -U $UsuarioDb -d $NombreDb --no-owner --no-privileges > $rutaArchivoSql
    } else {
        Write-Error "No se encontro ni 'pg_dump' ni 'docker' en el PATH. Imposible generar el respaldo."
        exit 1
    }
}

if (-not (Test-Path $rutaArchivoSql) -or (Get-Item $rutaArchivoSql).Length -eq 0) {
    Write-Error "Fallo en la generacion del archivo de respaldo SQL."
    exit 1
}

Write-Output "2. Comprimiendo volcado SQL con Gzip..."
$bytesEntrada = [System.IO.File]::ReadAllBytes($rutaArchivoSql)
$flujoSalida = [System.IO.File]::Create($rutaArchivoGz)
$flujoGzip = [System.IO.Compression.GZipStream]::new($flujoSalida, [System.IO.Compression.CompressionLevel]::Optimal)
$flujoGzip.Write($bytesEntrada, 0, $bytesEntrada.Length)
$flujoGzip.Close()
$flujoSalida.Close()

# Eliminar archivo SQL sin comprimir para ahorrar espacio
Remove-Item -Path $rutaArchivoSql -Force

Write-Output "3. Calculando checksum criptografico SHA-256 para validacion de integridad..."
$bytesGz = [System.IO.File]::ReadAllBytes($rutaArchivoGz)
$sha256 = [System.Security.Cryptography.SHA256]::Create()
$hashBytes = $sha256.ComputeHash($bytesGz)
$hashHex = -join ($hashBytes | ForEach-Object { $_.ToString("x2") })

$lineaChecksum = "$hashHex  $(Split-Path -Leaf $rutaArchivoGz)"
[System.IO.File]::WriteAllText($rutaArchivoChecksum, $lineaChecksum, [System.Text.Encoding]::UTF8)

Write-Output "   Checksum SHA-256: $hashHex"
Write-Output "   Tamano Comprimido: $((Get-Item $rutaArchivoGz).Length) bytes"

Write-Output "4. Aplicando politica de rotacion de respaldos ($DiasRetencion dias)..."
$fechaLimite = (Get-Date).AddDays(-$DiasRetencion)
$archivosViejos = Get-ChildItem -Path $DirectorioSalida -Filter "respaldo_ddtextil_*.sql.gz*" | Where-Object { $_.LastWriteTime -lt $fechaLimite }

$eliminados = 0
foreach ($archivo in $archivosViejos) {
    Remove-Item -Path $archivo.FullName -Force
    $eliminados++
}
Write-Output "   Archivos antiguos eliminados por expiracion: $eliminados"

Write-Output "================================================================================"
Write-Output "RESPALDO COMPLETADO EXITOSAMENTE"
Write-Output "Archivo generado: $rutaArchivoGz"
Write-Output "================================================================================"
