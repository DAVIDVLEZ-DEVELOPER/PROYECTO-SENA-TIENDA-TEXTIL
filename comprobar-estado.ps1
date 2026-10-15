# Script de verificación de estado y diagnóstico para D&D Textil
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "     D&D Textil - Verificación de Estado y Puertos        " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$ports = @(
    @{ Name = "Frontend (Nginx/React)"; Port = 3001 },
    @{ Name = "Backend (Java API)";     Port = 8081 },
    @{ Name = "PostgreSQL (BD)";         Port = 5432 }
)

Write-Host "`n[1] Verificando puertos en el sistema operativo (Windows)..." -ForegroundColor Yellow
foreach ($p in $ports) {
    $port = $p.Port
    $name = $p.Name
    $netstat = netstat -ano | Select-String ":$port\s"
    if ($netstat) {
        Write-Host "  -> Puerto $port ($name): EN USO" -ForegroundColor Green
        foreach ($line in $netstat) {
            $parts = ($line.Line.Trim() -split '\s+')
            $pidNumber = $parts[-1]
            try {
                $proc = Get-Process -Id $pidNumber -ErrorAction SilentlyContinue
                Write-Host "     Escuchando: $($parts[1]) | Proceso: $($proc.ProcessName) (PID: $pidNumber)" -ForegroundColor Gray
            } catch {}
        }
    } else {
        Write-Host "  -> Puerto $port ($name): LIBRE (Contenedor apagado o no mapeado)" -ForegroundColor Red
    }
}

Write-Host "`n[2] Probando conexión HTTP a Frontend..." -ForegroundColor Yellow
$frontendOk = $false
try {
    $res = curl.exe -s -o /dev/null -w "%{http_code}" -m 3 http://localhost:3001
    if ($res -eq "200") {
        Write-Host "  -> http://localhost:3001 Responde correctamente (HTTP 200 OK)" -ForegroundColor Green
        $frontendOk = $true
    } else {
        Write-Host "  -> http://localhost:3001 Código recibido: $res" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  -> No se pudo conectar a http://localhost:3001" -ForegroundColor Red
}

Write-Host "`n[3] Probando conexión HTTP a Backend Health Check..." -ForegroundColor Yellow
$backendOk = $false
try {
    $resB = curl.exe -s -o /dev/null -w "%{http_code}" -m 3 http://localhost:8081/health/live
    if ($resB -eq "200") {
        Write-Host "  -> http://localhost:8081/health/live Responde correctamente (HTTP 200 OK)" -ForegroundColor Green
        $backendOk = $true
    } else {
        Write-Host "  -> Backend en 8081 código recibido: $resB" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  -> No se pudo conectar a http://localhost:8081" -ForegroundColor Red
}

if (-not $frontendOk -or -not $backendOk) {
    Write-Host "`n[!] Detectando posibles conflictos de socket con Windows/WSL (dllhost en [::1])..." -ForegroundColor Yellow
    $conflicts = netstat -ano | Select-String "\[::1\]:(3001|8081|5432)\s"
    if ($conflicts) {
        foreach ($c in $conflicts) {
            $parts = ($c.Line.Trim() -split '\s+')
            $cPid = $parts[-1]
            try {
                $proc = Get-Process -Id $cPid -ErrorAction SilentlyContinue
                if ($proc -and $proc.ProcessName -eq "dllhost") {
                    Write-Host "  -> Conflicto detectado: dllhost.exe (PID $cPid) secuestrando $($parts[1])" -ForegroundColor Red
                    Write-Host "  -> Terminando proceso conflictivo PID $cPid para liberar localhost..." -ForegroundColor Cyan
                    Stop-Process -Id $cPid -Force -ErrorAction SilentlyContinue
                    Start-Sleep -Seconds 1
                    Write-Host "  -> Conflicto liberado exitosamente." -ForegroundColor Green
                }
            } catch {}
        }
    }
}

Write-Host "`nDiagnóstico completado." -ForegroundColor Cyan
