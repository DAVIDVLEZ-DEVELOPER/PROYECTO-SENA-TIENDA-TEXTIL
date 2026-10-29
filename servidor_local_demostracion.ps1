<#
.SYNOPSIS
    Servidor Local de Demostracion en Vivo (Nativo Windows / .NET HttpListener) - D&D Textil
.DESCRIPTION
    Levanta un servidor web y API REST local en los puertos 3001 y 8081 sin requerir
    la instalacion de Docker, Node.js ni Java. Sirve los activos compilados de la SPA
    y responde a los endpoints del backend para navegacion y sustentacion interactiva.
#>

param(
    [int]$PuertoFrontend = 3001,
    [int]$PuertoBackend = 8081,
    [switch]$NoAbrirNavegador
)

$ErrorActionPreference = "Continue"

$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$assetsDir = Join-Path $rootDir "TiendaTextilApp\app\src\main\assets"

if (-not (Test-Path $assetsDir)) {
    Write-Error "No se encontraron los activos compilados en: $assetsDir"
    exit 1
}

Write-Output "================================================================================"
Write-Output "SERVIDOR LOCAL DE DEMOSTRACION EN VIVO - D&D TEXTIL"
Write-Output "Iniciando servidor nativo .NET HttpListener..."
Write-Output "Frontend Web: http://localhost:${PuertoFrontend}/"
Write-Output "Backend API:  http://localhost:${PuertoBackend}/"
Write-Output "================================================================================"

$listener = New-Object System.Net.HttpListener
$prefixFrontend = "http://localhost:${PuertoFrontend}/"
$prefixBackend = "http://localhost:${PuertoBackend}/"

$listener.Prefixes.Add($prefixFrontend)
$listener.Prefixes.Add($prefixBackend)

try {
    $listener.Start()
} catch {
    Write-Error "No se pudo iniciar HttpListener: $_"
    exit 1
}

Write-Output "Servidor activo y escuchando peticiones HTTP."
if (-not $NoAbrirNavegador) {
    Start-Process "http://localhost:${PuertoFrontend}/"
}

$mimeMap = @{
    ".html" = "text/html; charset=UTF-8"
    ".js"   = "application/javascript; charset=UTF-8"
    ".css"  = "text/css; charset=UTF-8"
    ".png"  = "image/png"
    ".jpg"  = "image/jpeg"
    ".jpeg" = "image/jpeg"
    ".webp" = "image/webp"
    ".svg"  = "image/svg+xml; charset=UTF-8"
    ".json" = "application/json; charset=UTF-8"
}

$productosJson = @'
[
  {"id":1,"name":"Algodon Premium","price":25000,"stock":100,"category":"Algodon","description":"Tela 100% natural para camisas y confeccion fina.","featured":true,"images":["/images/hero-telas-premium.png"],"material":"Algodon 100%","width":"1.50m"},
  {"id":2,"name":"Seda Italiana","price":85000,"stock":50,"category":"Seda","description":"Seda pura con acabado brillante para vestidos de gala.","featured":true,"images":["/images/carousel-telas-exclusivas.png"],"material":"Seda pura","width":"1.40m"},
  {"id":3,"name":"Lino Organico","price":45000,"stock":80,"category":"Lino","description":"Lino transpirable de alta frescura para cortinas y ropa de verano.","featured":true,"images":["/images/carousel-nueva-coleccion.png"],"material":"Lino 100%","width":"1.50m"},
  {"id":4,"name":"Lana Merino","price":65000,"stock":40,"category":"Lana","description":"Lana calida y suave para abrigos y trajes de invierno.","featured":false,"images":["/images/hero-telas-premium.png"],"material":"Lana Merino","width":"1.45m"},
  {"id":5,"name":"Poliester Deportivo","price":18000,"stock":150,"category":"Poliester","description":"Tejido de secado rapido para ropa deportiva.","featured":true,"images":["/images/carousel-ofertas-especiales.png"],"material":"Poliester 100%","width":"1.60m"},
  {"id":6,"name":"Terciopelo Azul Noche","price":95000,"stock":30,"category":"Terciopelo","description":"Terciopelo de gran sofisticacion para tapiceria y alta costura.","featured":true,"images":["/images/carousel-telas-exclusivas.png"],"material":"Poliester/Algodon","width":"1.40m"}
]
'@

$carruselJson = @'
[
  {"id":1,"title":"Nueva Coleccion 2026","subtitle":"Tendencias exclusivas en telas premium","image":"/images/carousel-nueva-coleccion.png","cta":"Ver Mas","section_key":"nuevas","active":true,"order":1},
  {"id":2,"title":"Telas Exclusivas","subtitle":"Disenos unicos para proyectos especiales","image":"/images/carousel-telas-exclusivas.png","cta":"Explorar","section_key":"exclusivas","active":true,"order":2},
  {"id":3,"title":"Ofertas Especiales","subtitle":"Descuentos por metraje y retazos en liquidacion","image":"/images/carousel-ofertas-especiales.png","cta":"Comprar","section_key":"ofertas","active":true,"order":3}
]
'@

while ($listener.IsListening) {
    try {
        $context = $listener.GetContext()
        $req = $context.Request
        $res = $context.Response

        $res.Headers.Add("Access-Control-Allow-Origin", "*")
        $res.Headers.Add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        $res.Headers.Add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Correlation-ID")

        if ($req.HttpMethod -eq "OPTIONS") {
            $res.StatusCode = 204
            $res.Close()
            continue
        }

        $urlPath = $req.Url.AbsolutePath

        # --- RESPUESTAS DE API REST (Backend) ---
        if ($urlPath -eq "/health/live" -or $urlPath -eq "/api/health/live") {
            $bytes = [System.Text.Encoding]::UTF8.GetBytes('{"status":"UP","service":"ddtextil-backend","uptime_ms":360000}')
            $res.ContentType = "application/json"
            $res.StatusCode = 200
            $res.OutputStream.Write($bytes, 0, $bytes.Length)
            $res.Close()
            continue
        }

        if ($urlPath -eq "/health/ready" -or $urlPath -eq "/api/health/ready") {
            $bytes = [System.Text.Encoding]::UTF8.GetBytes('{"status":"UP","database":"CONNECTED","checks":[{"name":"postgresql","status":"UP"}]}')
            $res.ContentType = "application/json"
            $res.StatusCode = 200
            $res.OutputStream.Write($bytes, 0, $bytes.Length)
            $res.Close()
            continue
        }

        if ($urlPath -eq "/api/pagos/config" -or $urlPath -eq "/api/payments/config") {
            $bytes = [System.Text.Encoding]::UTF8.GetBytes('{"configured":true,"publicKey":"pub_test_Q5yDA9xoKdePzhSGeVe9HAUr19rhZCQSQ","currency":"COP","environment":"sandbox"}')
            $res.ContentType = "application/json"
            $res.StatusCode = 200
            $res.OutputStream.Write($bytes, 0, $bytes.Length)
            $res.Close()
            continue
        }

        if ($urlPath -eq "/api/products" -or $urlPath -eq "/api/productos") {
            $bytes = [System.Text.Encoding]::UTF8.GetBytes($productosJson)
            $res.ContentType = "application/json"
            $res.StatusCode = 200
            $res.OutputStream.Write($bytes, 0, $bytes.Length)
            $res.Close()
            continue
        }

        if ($urlPath -eq "/api/carousel" -or $urlPath -eq "/api/carrusel") {
            $bytes = [System.Text.Encoding]::UTF8.GetBytes($carruselJson)
            $res.ContentType = "application/json"
            $res.StatusCode = 200
            $res.OutputStream.Write($bytes, 0, $bytes.Length)
            $res.Close()
            continue
        }

        if ($urlPath -eq "/api/auth/login" -or $urlPath -eq "/api/login") {
            $resJson = '{"token":"demo-jwt-token-sena-adso-2026","user":{"id":1,"name":"Anderson Moreno","email":"admin@ddtextil.com","role":"administrador"}}'
            $bytes = [System.Text.Encoding]::UTF8.GetBytes($resJson)
            $res.ContentType = "application/json"
            $res.StatusCode = 200
            $res.OutputStream.Write($bytes, 0, $bytes.Length)
            $res.Close()
            continue
        }

        # --- ARCHIVOS ESTATICOS DEL FRONTEND (SPA) ---
        $relPath = $urlPath.TrimStart('/')
        if ([string]::IsNullOrWhiteSpace($relPath)) {
            $relPath = "index.html"
        }

        $localFile = Join-Path $assetsDir $relPath
        if (-not (Test-Path $localFile) -or (Get-Item $localFile) -is [System.IO.DirectoryInfo]) {
            # Soporte de imagenes compartidas de /images/
            $sharedImg = Join-Path $rootDir "images\$relPath"
            if (Test-Path $sharedImg) {
                $localFile = $sharedImg
            } else {
                # Fallback SPA a index.html para rutas React Router (/admin, /cliente, etc.)
                $localFile = Join-Path $assetsDir "index.html"
            }
        }

        $ext = [System.IO.Path]::GetExtension($localFile).ToLower()
        $contentType = if ($mimeMap.ContainsKey($ext)) { $mimeMap[$ext] } else { "application/octet-stream" }

        $bytes = [System.IO.File]::ReadAllBytes($localFile)
        $res.ContentType = $contentType
        $res.StatusCode = 200
        $res.ContentLength64 = $bytes.Length
        $res.OutputStream.Write($bytes, 0, $bytes.Length)
        $res.Close()

    } catch {
        # Si se interrumpe la ejecucion o se cierra el socket
        if (-not $listener.IsListening) { break }
    }
}

$listener.Close()
Write-Output "Servidor detenido."
