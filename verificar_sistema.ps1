<#
.SYNOPSIS
    Script de Verificacion Pre-Vuelo (Preflight Health Check) - D&D Textil
.DESCRIPTION
    Comprueba en segundos el estado del entorno, conectividad de puertos,
    probes de observabilidad y preparacion para sustentacion o despliegue.
#>

param(
    [string]$BackendUrl = "http://localhost:8081",
    [string]$FrontendUrl = "http://localhost:3001",
    [string]$HostDb = "localhost",
    [int]$PuertoDb = 5432
)

$ErrorActionPreference = "Continue"

Write-Output "================================================================================"
Write-Output "VERIFICACION PRE-VUELO DEL SISTEMA - D&D TEXTIL"
Write-Output "Fecha y Hora: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output "================================================================================"

$totalChecks = 0
$checksExitosos = 0

function Evaluar-Check($nombre, $condicion, $mensajeDetalle) {
    $script:totalChecks++
    if ($condicion) {
        $script:checksExitosos++
        Write-Output "  [OK] $($nombre): $mensajeDetalle"
    } else {
        Write-Output "  [ALERTA] $($nombre): $mensajeDetalle"
    }
}

# 1. Verificacion de Archivo de Configuracion .env
Write-Output "`n[1. Configuracion y Variables de Entorno]"
$rutaEnv = Join-Path $PSScriptRoot ".env"
$existeEnv = Test-Path $rutaEnv
Evaluar-Check "Archivo .env" $existeEnv $(if ($existeEnv) { "Presente en la raiz" } else { "No encontrado. Ejecutar: cp .env.example .env" })

# 2. Verificacion de Conectividad a Puertos de Red
Write-Output "`n[2. Disponibilidad de Puertos de Red]"

function Test-Puerto($hostDestino, $puertoDestino) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $iar = $tcp.BeginConnect($hostDestino, $puertoDestino, $null, $null)
        $espera = $iar.AsyncWaitHandle.WaitOne(1000, $false)
        if ($espera) {
            $tcp.EndConnect($iar)
            $tcp.Close()
            return $true
        }
        $tcp.Close()
        return $false
    } catch {
        return $false
    }
}

$puertoDbActivo = Test-Puerto $HostDb $PuertoDb
Evaluar-Check "Puerto PostgreSQL ($PuertoDb)" $puertoDbActivo $(if ($puertoDbActivo) { "Conexion TCP establecida" } else { "Puerto cerrado o servicio inactivo" })

$puertoBackendActivo = Test-Puerto "localhost" 8081
Evaluar-Check "Puerto Backend Java (8081)" $puertoBackendActivo $(if ($puertoBackendActivo) { "Conexion TCP establecida" } else { "Servidor API no responde en puerto 8081" })

$puertoFrontendActivo = Test-Puerto "localhost" 3001
Evaluar-Check "Puerto Frontend Web (3001)" $puertoFrontendActivo $(if ($puertoFrontendActivo) { "Conexion TCP establecida" } else { "Servidor frontend no responde en puerto 3001" })

# 3. Verificacion de Probes HTTP de Observabilidad
Write-Output "`n[3. Probes de Observabilidad del Backend]"

if ($puertoBackendActivo) {
    try {
        $resLive = Invoke-RestMethod -Uri "$BackendUrl/health/live" -TimeoutSec 2
        Evaluar-Check "Liveness Probe (/health/live)" ($resLive.status -eq "UP") "Estado reportado: $($resLive.status)"
    } catch {
        Evaluar-Check "Liveness Probe (/health/live)" $false "Error: $_"
    }

    try {
        $resReady = Invoke-RestMethod -Uri "$BackendUrl/health/ready" -TimeoutSec 2
        Evaluar-Check "Readiness Probe (/health/ready)" ($resReady.status -eq "UP") "Estado: $($resReady.status) (DB: $($resReady.database))"
    } catch {
        Evaluar-Check "Readiness Probe (/health/ready)" $false "Error: $_"
    }

    try {
        $resWompi = Invoke-RestMethod -Uri "$BackendUrl/api/pagos/config" -TimeoutSec 2
        Evaluar-Check "Configuracion Pasarela Wompi" ($resWompi.currency -eq "COP") "Moneda: $($resWompi.currency), Ambiente: $($resWompi.environment)"
    } catch {
        Evaluar-Check "Configuracion Pasarela Wompi" $false "Error al consultar /api/pagos/config"
    }
} else {
    Write-Output "  [OMITIDO] Probes HTTP omitidos porque el backend no esta levantado."
}

# 4. Verificacion de Credenciales y Base de Datos
Write-Output "`n[4. Credenciales de Prueba (ISO/IEC 25010)]"
Write-Output "  - Administrador: admin@ddtextil.com     / admin123     (Panel /admin)"
Write-Output "  - Vendedor:      vendedor@ddtextil.com  / vendedor123  (Panel /vendedor)"
Write-Output "  - Cliente Demo:  cliente@ddtextil.com   / cliente123   (Panel /cliente)"
Write-Output "  - Cliente Alt:   cliente@gmail.com      / cliente123   (Panel /cliente)"

# 5. Dictamen Consolidado
Write-Output "`n================================================================================"
Write-Output "RESUMEN DE VERIFICACION PRE-VUELO"
Write-Output "Comprobaciones Evaluadas: $totalChecks"
Write-Output "Comprobaciones Exitosas: $checksExitosos"
if ($checksExitosos -eq $totalChecks -and $totalChecks -gt 0) {
    Write-Output "DICTAMEN: ENTORNO COMPLETAMENTE OPERATIVO PARA SUSTENTACION Y PRODUCCION"
} else {
    Write-Output "DICTAMEN: VERIFICAR SERVICIOS INACTIVOS MEDIANTE 'docker compose up -d'"
}
Write-Output "================================================================================"
