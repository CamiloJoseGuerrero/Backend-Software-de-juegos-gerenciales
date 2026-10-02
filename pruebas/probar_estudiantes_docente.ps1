# =====================================================================
#  Prueba: estudiantes por docente, alta manual, vinculación y caso-actual
#  Uso (PowerShell, con el backend corriendo y la migración docente_estudiante ejecutada):
#     powershell -ExecutionPolicy Bypass -File .\pruebas\probar_estudiantes_docente.ps1
#  Opcional: -Base "https://xxxx.ngrok-free.dev/api"   -Docente "otro@test.com" -ClaveDocente "..."
#  Cada corrida registra un docente B nuevo y un estudiante nuevo, así que se puede repetir.
# =====================================================================
param(
    [string]$Base = "http://localhost:8080/api",
    [string]$Docente = "nuevo@test.com",
    [string]$ClaveDocente = "Password1!"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [Text.Encoding]::UTF8
[Net.ServicePointManager]::Expect100Continue = $false
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$excel = Join-Path $PSScriptRoot "estudiantes_vincular.xlsx"
# Estudiantes que ya existen (los del Excel estudiantes_validos)
$andres = @{ id = 17; correo = "andres.martinez.prueba@correo.edu.co" }
$maria  = @{ id = 20; correo = "maria.castillo.prueba@correo.edu.co" }

$script:ok = 0; $script:fallos = 0

function Api($metodo, $ruta, $token, $datos) {
    $h = @{ "ngrok-skip-browser-warning" = "true" }
    if ($token) { $h.Authorization = "Bearer $token" }
    $p = @{ Uri = "$Base$ruta"; Method = $metodo; Headers = $h; UseBasicParsing = $true }
    if ($null -ne $datos) {
        $p.ContentType = "application/json; charset=utf-8"
        $p.Body = [Text.Encoding]::UTF8.GetBytes(($datos | ConvertTo-Json -Depth 6))
    }
    try {
        $r = Invoke-WebRequest @p
        $texto = [Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
        $status = [int]$r.StatusCode
    } catch {
        $resp = $_.Exception.Response
        if ($null -eq $resp) { throw "No hay conexión con $Base : $($_.Exception.Message)" }
        $status = [int]$resp.StatusCode
        $texto = (New-Object IO.StreamReader($resp.GetResponseStream(), [Text.Encoding]::UTF8)).ReadToEnd()
    }
    $cuerpo = $null
    if ($texto) { try { $cuerpo = $texto | ConvertFrom-Json } catch { $cuerpo = $texto } }
    [pscustomobject]@{ Status = $status; Body = $cuerpo }
}

# Subida del Excel con curl.exe (viene con Windows 10/11)
function CargaMasiva($token) {
    $tipo = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    $salida = & curl.exe -s -w "`n%{http_code}" -H "Authorization: Bearer $token" -H "ngrok-skip-browser-warning: true" `
        -F "archivo=@$excel;type=$tipo" "$Base/docente/estudiantes/carga-masiva"
    $lineas = @($salida)
    $status = [int]$lineas[-1]
    $json = ($lineas[0..($lineas.Count - 2)] -join "`n")
    $cuerpo = $null
    if ($json) { try { $cuerpo = $json | ConvertFrom-Json } catch { $cuerpo = $json } }
    [pscustomobject]@{ Status = $status; Body = $cuerpo }
}

function Check($nombre, [bool]$condicion, $detalle) {
    if ($condicion) { $script:ok++; Write-Host "  [OK]    $nombre" -ForegroundColor Green }
    else { $script:fallos++; Write-Host "  [FALLA] $nombre" -ForegroundColor Red; if ($detalle) { Write-Host "          $detalle" -ForegroundColor DarkGray } }
}

function Esperar($r, $status, $nombre) {
    $msg = if ($r.Body -and $r.Body.message) { $r.Body.message } else { "" }
    Check $nombre ($r.Status -eq $status) "esperado $status, llegó $($r.Status) $msg"
    if ($r.Status -ge 400 -and $r.Status -eq $status -and $msg) { Write-Host "          -> $msg" -ForegroundColor DarkGray }
}

function Login($correo, $clave) {
    $r = Api Post "/auth/login" $null @{ correo = $correo; contrasena = $clave }
    if ($r.Status -ne 200) { throw "No se pudo iniciar sesión con $correo ($($r.Status)): $($r.Body.message)" }
    $r.Body.token
}

function Ids($r) { @($r.Body | ForEach-Object { [long]$_.id }) }

$fmt = "yyyy-MM-ddTHH:mm:ss"
$ahora = Get-Date
$sello = $ahora.ToString("MMddHHmmss")
Write-Host "`nProbando contra $Base`n" -ForegroundColor Cyan

# ---------------------------------------------------------------------
Write-Host "1. Docentes" -ForegroundColor Cyan
$tA = Login $Docente $ClaveDocente
Check "login docente A ($Docente)" ($null -ne $tA)
$correoB = "docente.b.$sello@estratego-test.local"
$claveB = "Password1!"
$reg = Api Post "/auth/registro-docente" $null @{ nombre = "Docente B $sello"; correo = $correoB; numeroIdentificacion = "DB$sello"; contrasena = $claveB }
Esperar $reg 201 "registrar docente B ($correoB)"
$tB = Login $correoB $claveB

# ---------------------------------------------------------------------
Write-Host "`n2. Cada docente ve solo sus estudiantes" -ForegroundColor Cyan
$listaB = Api Get "/docente/estudiantes" $tB
Check "docente B nuevo: lista vacía" ($listaB.Status -eq 200 -and @($listaB.Body).Count -eq 0) "status $($listaB.Status), $(@($listaB.Body).Count) estudiantes"
$listaA = Api Get "/docente/estudiantes" $tA
Check "docente A ve su lista ($(@($listaA.Body).Count) estudiantes)" ($listaA.Status -eq 200)
$primeroA = (Ids $listaA) | Select-Object -First 1
if ($primeroA) { Esperar (Api Get "/docente/estudiantes/$primeroA" $tB) 400 "B pide un estudiante de A -> 400 (no lo ve)" }

# ---------------------------------------------------------------------
Write-Host "`n3. Alta manual (docente B)" -ForegroundColor Cyan
$cedula = "77$sello"
$nuevo = @{ nombre = "Estudiante Prueba $sello"; correo = "est.$sello@estratego-test.local"; numeroIdentificacion = $cedula; edad = 20; genero = "F" }
$alta = Api Post "/docente/estudiantes" $tB $nuevo
Esperar $alta 201 "crear estudiante nuevo -> 201"
Check "devuelve la contraseña generada" ($alta.Body.contrasenaGenerada -eq "Usu-001-$cedula!")
$idNuevo = [long]$alta.Body.id
Esperar (Api Post "/docente/estudiantes" $tB $nuevo) 409 "el mismo otra vez -> 409 (ya está en tu lista)"
$otraCedula = $nuevo.Clone(); $otraCedula.numeroIdentificacion = "88$sello"
Esperar (Api Post "/docente/estudiantes" $tB $otraCedula) 409 "mismo correo con otra cédula -> 409"
$malo = $nuevo.Clone(); $malo.correo = "x.$sello@estratego-test.local"; $malo.numeroIdentificacion = "99$sello"; $malo.genero = "X"
Esperar (Api Post "/docente/estudiantes" $tB $malo) 400 "género inválido -> 400"
$listaB = Api Get "/docente/estudiantes" $tB
Check "B ve 1 estudiante (el nuevo)" (@(Ids $listaB).Count -eq 1 -and (Ids $listaB) -contains $idNuevo)

# ---------------------------------------------------------------------
Write-Host "`n4. El docente A no lo ve hasta vincularlo" -ForegroundColor Cyan
Check "no aparece en la lista de A" (-not ((Ids (Api Get "/docente/estudiantes" $tA)) -contains $idNuevo))
Esperar (Api Get "/docente/estudiantes/$idNuevo" $tA) 400 "A pide el detalle -> 400"
$vinc = Api Post "/docente/estudiantes" $tA $nuevo
Esperar $vinc 200 "A lo agrega a mano -> 200 (vinculado, sin cuenta nueva)"
Check "vinculado: sin contraseña y mismo id" ($null -eq $vinc.Body.contrasenaGenerada -and [long]$vinc.Body.id -eq $idNuevo)
Check "ahora aparece en la lista de A" ((Ids (Api Get "/docente/estudiantes" $tA)) -contains $idNuevo)
Esperar (Api Get "/docente/estudiantes/$idNuevo" $tA) 200 "A ve el detalle -> 200"

# ---------------------------------------------------------------------
Write-Host "`n5. Carga masiva que vincula (docente B, $([IO.Path]::GetFileName($excel)))" -ForegroundColor Cyan
if (-not (Test-Path $excel)) { throw "No encuentro $excel" }
$cm = CargaMasiva $tB
Esperar $cm 200 "subir Excel"
Check "0 creados, 2 vinculados (Andrés y Laura ya existían)" (@($cm.Body.creados).Count -eq 0 -and @($cm.Body.vinculados).Count -eq 2) "creados $(@($cm.Body.creados).Count), vinculados $(@($cm.Body.vinculados).Count)"
Check "1 error (Santiago con otra cédula)" (@($cm.Body.errores).Count -eq 1) "errores $(@($cm.Body.errores).Count)"
$cm2 = CargaMasiva $tB
Check "subirlo otra vez: 0 vinculados, 3 errores" (@($cm2.Body.vinculados).Count -eq 0 -and @($cm2.Body.errores).Count -eq 3) "vinculados $(@($cm2.Body.vinculados).Count), errores $(@($cm2.Body.errores).Count)"
Check "B ahora ve 3 estudiantes" (@(Ids (Api Get "/docente/estudiantes" $tB)).Count -eq 3)

# ---------------------------------------------------------------------
Write-Host "`n6. Integrantes: solo estudiantes propios" -ForegroundColor Cyan
$simY = Api Post "/docente/simulaciones" $tB @{ nombre = "Prueba estudiantes Y $sello"; fechaInicio = $ahora.ToString("yyyy-MM-dd"); fechaFin = $ahora.AddDays(30).ToString("yyyy-MM-dd") }
Esperar $simY 201 "simulación Y (empieza hoy)"
$simX = Api Post "/docente/simulaciones" $tB @{ nombre = "Prueba estudiantes X $sello"; fechaInicio = $ahora.AddDays(1).ToString("yyyy-MM-dd"); fechaFin = $ahora.AddDays(30).ToString("yyyy-MM-dd") }
Esperar $simX 201 "simulación X (empieza mañana)"
$idY = $simY.Body.id; $idX = $simX.Body.id
$empY = (Api Post "/docente/simulaciones/$idY/empresas" $tB @{ nombre = "Empresa Y"; tipoJugador = "MULTIUSUARIO" }).Body.id
$empX = (Api Post "/docente/simulaciones/$idX/empresas" $tB @{ nombre = "Empresa X"; tipoJugador = "MULTIUSUARIO" }).Body.id
Esperar (Api Post "/docente/empresas/$empY/integrantes" $tB @{ idUsuario = $maria.id }) 400 "agregar a María (no es de B) -> 400"
Esperar (Api Post "/docente/empresas/$empY/integrantes" $tB @{ idUsuario = $andres.id }) 201 "agregar a Andrés (vinculado por Excel) -> 201"
Esperar (Api Post "/docente/empresas/$empY/integrantes" $tB @{ idUsuario = $idNuevo; esLider = $true }) 201 "estudiante nuevo líder en Y"
Esperar (Api Post "/docente/empresas/$empX/integrantes" $tB @{ idUsuario = $idNuevo; esLider = $true }) 201 "estudiante nuevo líder en X"

# ---------------------------------------------------------------------
Write-Host "`n7. caso-actual con dos simulaciones" -ForegroundColor Cyan
function NuevoCaso($idSim, $nombre, $vis, $ini, $fin) {
    @{ idSimulacion = $idSim; nombre = $nombre; tipo = "Prueba"; mision = "m"; vision = "v"
       financiero = @{ activoTotal = 1000; pasivoTotal = 400; patrimonio = 600; utilidadNeta = 50; ventasNetas = 800; costoVentas = 500; gastosOperativos = 200 }
       penalizacionMin = 1; penalizacionMax = 5
       fechaVisualizacion = $vis.ToString($fmt); fechaInicioPartida = $ini.ToString($fmt); fechaFinPartida = $fin.ToString($fmt)
       opciones = @(@{ opcion = "A"; resultado = "ra" }, @{ opcion = "B"; resultado = "rb" }) }
}
$iniY = $ahora.AddMinutes(-10); if ($iniY.Date -lt $ahora.Date) { $iniY = $ahora.AddSeconds(5) }
$cY = Api Post "/docente/casos" $tB (NuevoCaso $idY "Caso Y en partida" $ahora.AddMinutes(-30) $iniY $ahora.AddHours(2))
Esperar $cY 201 "caso Y: partida en curso"
$manana = $ahora.Date.AddDays(1)
$cX = Api Post "/docente/casos" $tB (NuevoCaso $idX "Caso X de mañana" $ahora.AddMinutes(-30) $manana.AddHours(9) $manana.AddHours(12))
Esperar $cX 201 "caso X: visible, empieza mañana"
Esperar (Api Post "/docente/casos/$($cY.Body.id)/activar" $tB) 200 "activar caso Y"
Esperar (Api Post "/docente/casos/$($cX.Body.id)/activar" $tB) 200 "activar caso X"
Esperar (Api Post "/docente/simulaciones/$idY/programar" $tB) 200 "programar Y"
Esperar (Api Post "/docente/simulaciones/$idX/programar" $tB) 200 "programar X"

$tEst = Login $nuevo.correo "Usu-001-$cedula!"
$ca = Api Get "/estudiante/caso-actual" $tEst
Check "sin idSimulacion elige el caso Y (puede decidir), no el X" ($ca.Status -eq 200 -and $ca.Body.caso.id -eq $cY.Body.id -and $ca.Body.puedeDecidir) "status $($ca.Status), caso $($ca.Body.caso.id) ($($ca.Body.caso.nombre)), puedeDecidir $($ca.Body.puedeDecidir)"
$caX = Api Get "/estudiante/caso-actual?idSimulacion=$idX" $tEst
Check "con idSimulacion=X devuelve el caso X (aún no puede decidir)" ($caX.Status -eq 200 -and $caX.Body.caso.id -eq $cX.Body.id -and -not $caX.Body.puedeDecidir) "status $($caX.Status), caso $($caX.Body.caso.id)"

# ---------------------------------------------------------------------
$color = if ($script:fallos -eq 0) { "Green" } else { "Red" }
Write-Host "`nResultado: $($script:ok) OK, $($script:fallos) fallas`n" -ForegroundColor $color
