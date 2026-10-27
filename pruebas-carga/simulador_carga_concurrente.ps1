param(
    [string]$BaseUrl = "http://localhost:8081",
    [int]$PeticionesTotales = 200,
    [int]$Concurrencia = 20
)

Write-Host "================================================================="
Write-Host "   SIMULADOR DE CARGA Y ESTRES CONCURRENTE (POWERSHELL / .NET)  "
Write-Host "================================================================="
Write-Host "Servidor Destino:    $BaseUrl"
Write-Host "Total Peticiones:    $PeticionesTotales"
Write-Host "Hilos Concurrentes:  $Concurrencia"
Write-Host ""

$handler = [System.Net.Http.HttpClientHandler]::new()
$client = [System.Net.Http.HttpClient]::new($handler)
$client.Timeout = [System.TimeSpan]::FromSeconds(10)

$endpoints = @(
    "/health/live",
    "/api/productos",
    "/api/carrusel",
    "/api/secciones-inicio",
    "/api/pagos/config"
)

$latencias = [System.Collections.Generic.List[double]]::new()
$conteoExitosos = [System.Threading.Interlocked]
$exitos = 0
$fallos = 0
$lockObj = [Object]::new()

$swTotal = [System.Diagnostics.Stopwatch]::StartNew()

$tasks = [System.Collections.Generic.List[System.Threading.Tasks.Task]]::new()
$semaphore = [System.Threading.SemaphoreSlim]::new($Concurrencia)

for ($i = 0; $i -lt $PeticionesTotales; $i++) {
    $ep = $endpoints[$i % $endpoints.Length]
    $url = "$BaseUrl$ep"

    $semaphore.Wait()

    $task = [System.Threading.Tasks.Task]::Run([Action]{
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            $req = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Get, $url)
            $req.Headers.Add("X-Correlation-ID", [System.Guid]::NewGuid().ToString())
            
            $resp = $client.SendAsync($req).GetAwaiter().GetResult()
            $sw.Stop()

            $ms = $sw.Elapsed.TotalMilliseconds
            [System.Threading.Monitor]::Enter($lockObj)
            try {
                $latencias.Add($ms)
                if ($resp.IsSuccessStatusCode) {
                    $script:exitos++
                } else {
                    $script:fallos++
                }
            } finally {
                [System.Threading.Monitor]::Exit($lockObj)
            }
        } catch {
            $sw.Stop()
            [System.Threading.Monitor]::Enter($lockObj)
            try {
                $script:fallos++
            } finally {
                [System.Threading.Monitor]::Exit($lockObj)
            }
        } finally {
            $semaphore.Release()
        }
    })
    $tasks.Add($task)
}

[System.Threading.Tasks.Task]::WaitAll($tasks.ToArray())
$swTotal.Stop()

$duracionSeg = $swTotal.Elapsed.TotalSeconds
$throughput = if ($duracionSeg -gt 0) { [Math]::Round($PeticionesTotales / $duracionSeg, 2) } else { 0 }

# Calculo de Percentiles
$latenciasArray = $latencias | Sort-Object
$conteo = $latenciasArray.Count

function ObtenerPercentil($arr, $p) {
    if ($arr.Count -eq 0) { return 0 }
    $idx = [Math]::Floor(($arr.Count - 1) * $p)
    return [Math]::Round($arr[$idx], 2)
}

$p50 = ObtenerPercentil $latenciasArray 0.50
$p90 = ObtenerPercentil $latenciasArray 0.90
$p95 = ObtenerPercentil $latenciasArray 0.95
$p99 = ObtenerPercentil $latenciasArray 0.99
$promedio = if ($conteo -gt 0) { [Math]::Round(($latenciasArray | Measure-Object -Average).Average, 2) } else { 0 }

Write-Host "================================================================="
Write-Host "                    METRICAS Y RESULTADOS                       "
Write-Host "================================================================="
Write-Host "Tiempo Total de Prueba:    $([Math]::Round($duracionSeg, 2)) segundos"
Write-Host "Peticiones Exitosas:       $exitos"
Write-Host "Peticiones Fallidas:       $fallos"
Write-Host "Rendimiento (Throughput):  $throughput req/s"
Write-Host "Latencia Promedio:         $promedio ms"
Write-Host "Percentil 50 (Mediana):    $p50 ms"
Write-Host "Percentil 90 (P90):        $p90 ms"
Write-Host "Percentil 95 (P95):        $p95 ms"
Write-Host "Percentil 99 (P99):        $p99 ms"
Write-Host "================================================================="

if ($fallos -eq 0) {
    Write-Host "ESTADO: PRUEBA DE CARGA SUPERADA CON 0% DE FALLOS"
} else {
    Write-Host "ESTADO: SE REGISTRARON FALLOS EN LA PRUEBA ($fallos fallos)"
}
