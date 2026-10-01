# =====================================================================
#  Prueba de punta a punta del backend Estratego
#  Uso (PowerShell, con el backend corriendo):
#     powershell -ExecutionPolicy Bypass -File .\pruebas\probar_api.ps1
#  Opcional: -Base "https://xxxx.ngrok-free.dev/api"   -Docente "otro@test.com" -ClaveDocente "..."
#  Cada corrida crea una simulación nueva ("Prueba script <fecha>"), así que se puede repetir.
# =====================================================================
param(
    [string]$Base = "http://localhost:8080/api",
    [string]$Docente = "nuevo@test.com",
    [string]$ClaveDocente = "Password1!"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [Text.Encoding]::UTF8
# PowerShell 5.1 envía "Expect: 100-continue" en peticiones con cuerpo; a través de ngrok eso
# hace que reporte 200 en vez del código real (201, 400...). Se desactiva.
[Net.ServicePointManager]::Expect100Continue = $false
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

# Estudiantes de la carga masiva (contraseña = Usu-001-<cédula>!)
$lider   = @{ id = 17; correo = "andres.martinez.prueba@correo.edu.co"; cedula = "1101000001" }
$miembro = @{ id = 18; correo = "laura.gomez.prueba@correo.edu.co";     cedula = "1101000002" }
$mono    = @{ id = 19; correo = "santiago.herrera.prueba@correo.edu.co"; cedula = "1101000003" }
$ajeno   = @{ id = 20; correo = "maria.castillo.prueba@correo.edu.co";  cedula = "1101000004" }

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

$fmt = "yyyy-MM-ddTHH:mm:ss"
$ahora = Get-Date
Write-Host "`nProbando contra $Base`n" -ForegroundColor Cyan

# ---------------------------------------------------------------------
Write-Host "1. Autenticación" -ForegroundColor Cyan
$tDoc = Login $Docente $ClaveDocente
Check "login docente $Docente" ($null -ne $tDoc)
Esperar (Api Post "/auth/login" $null @{ correo = $Docente; contrasena = "incorrecta" }) 401 "contraseña incorrecta -> 401"
$ses = Api Get "/auth/sesion" $tDoc
Check "GET /auth/sesion devuelve rol DOCENTE" ($ses.Status -eq 200 -and $ses.Body.usuario.rol -eq "DOCENTE")
Esperar (Api Get "/docente/estudiantes" $null) 403 "sin token -> 403"

# ---------------------------------------------------------------------
Write-Host "`n2. Estudiantes" -ForegroundColor Cyan
$est = Api Get "/docente/estudiantes" $tDoc
Check "listar estudiantes" ($est.Status -eq 200 -and @($est.Body).Count -gt 0) "status $($est.Status)"
$e1 = Api Get "/docente/estudiantes/$($lider.id)" $tDoc
Check "detalle del estudiante $($lider.id)" ($e1.Status -eq 200 -and $e1.Body.correo -eq $lider.correo)
Esperar (Api Get "/docente/estudiantes/3" $tDoc) 400 "detalle de un docente -> 400"

# ---------------------------------------------------------------------
Write-Host "`n3. Simulación" -ForegroundColor Cyan
$sim = Api Post "/docente/simulaciones" $tDoc @{ nombre = "Prueba script $($ahora.ToString('yyyy-MM-dd HH:mm'))"; fechaInicio = $ahora.ToString("yyyy-MM-dd"); fechaFin = $ahora.AddDays(30).ToString("yyyy-MM-dd") }
Esperar $sim 201 "crear simulación (BORRADOR)"
$idSim = $sim.Body.id
Esperar (Api Post "/docente/simulaciones" $tDoc @{ nombre = "Pasada"; fechaInicio = $ahora.AddDays(-1).ToString("yyyy-MM-dd") }) 400 "fecha de inicio pasada -> 400"

# ---------------------------------------------------------------------
Write-Host "`n4. Empresas" -ForegroundColor Cyan
$empA = Api Post "/docente/simulaciones/$idSim/empresas" $tDoc @{ nombre = "Empresa Multi"; tipoJugador = "MULTIUSUARIO" }
Esperar $empA 201 "crear empresa MULTIUSUARIO"
Check "código EMP-001" ($empA.Body.codigoEmpresa -eq "EMP-001")
$empM = Api Post "/docente/simulaciones/$idSim/empresas" $tDoc @{ nombre = "Empresa Mono"; tipoJugador = "MONOUSUARIO" }
Esperar $empM 201 "crear empresa MONOUSUARIO"
$idA = $empA.Body.id; $idM = $empM.Body.id

# ---------------------------------------------------------------------
Write-Host "`n5. Integrantes" -ForegroundColor Cyan
Esperar (Api Post "/docente/empresas/$idA/integrantes" $tDoc @{ idUsuario = $lider.id; departamento = "COMERCIAL"; esLider = $true }) 201 "agregar líder a Empresa Multi"
$i2 = Api Post "/docente/empresas/$idA/integrantes" $tDoc @{ idUsuario = $miembro.id }
Esperar $i2 201 "agregar integrante sin departamento"
Check "departamento por defecto GERENCIA_GENERAL" ($i2.Body.departamento -eq "GERENCIA_GENERAL" -and -not $i2.Body.esLider)
$im = Api Post "/docente/empresas/$idM/integrantes" $tDoc @{ idUsuario = $mono.id }
Check "MONOUSUARIO: el único integrante queda líder" ($im.Status -eq 201 -and $im.Body.esLider)
Esperar (Api Post "/docente/empresas/$idM/integrantes" $tDoc @{ idUsuario = $ajeno.id }) 400 "MONOUSUARIO con 2 integrantes -> 400"
Esperar (Api Post "/docente/empresas/$idM/integrantes" $tDoc @{ idUsuario = $lider.id }) 400 "mismo estudiante en 2 empresas -> 400"
Esperar (Api Post "/docente/empresas/$idA/integrantes" $tDoc @{ idUsuario = 3 }) 400 "un docente como integrante -> 400"
Esperar (Api Put "/docente/empresas/$idA/integrantes/$($miembro.id)" $tDoc @{ departamento = "PRODUCCION" }) 400 "departamento inválido -> 400"
Esperar (Api Put "/docente/empresas/$idA/integrantes/$($miembro.id)/lider" $tDoc) 200 "cambiar líder"
$lista = (Api Get "/docente/empresas/$idA/integrantes" $tDoc).Body
Check "solo un líder en la empresa" (@($lista | Where-Object { $_.esLider }).Count -eq 1)
Esperar (Api Put "/docente/empresas/$idA/integrantes/$($lider.id)/lider" $tDoc) 200 "devolver el liderazgo"

# ---------------------------------------------------------------------
Write-Host "`n6. Casos" -ForegroundColor Cyan
$caso = @{
    idSimulacion = $idSim; nombre = "TextilAndes S.A."; tipo = "Manufactura"
    mision = "Vestir a Colombia"; vision = "Líder regional en 2030"
    financiero = @{ activoTotal = 1000000; pasivoTotal = 400000; patrimonio = 600000; utilidadNeta = 50000 }
    penalizacionMin = 2; penalizacionMax = 8
    fechaVisualizacion = $ahora.AddMinutes(-30).ToString($fmt)
    fechaInicioPartida = $ahora.AddMinutes(-10).ToString($fmt)
    fechaFinPartida    = $ahora.AddHours(2).ToString($fmt)
    asignacionEquipos = "manual"
    opciones = @(@{ opcion = "Ampliar la planta"; resultado = "Sube la capacidad 20%" },
                 @{ opcion = "Reducir costos";    resultado = "Mejora el margen 5%" })
}
# Si la prueba corre justo después de medianoche, la partida quedaría antes del inicio de la simulación
if ($ahora.AddMinutes(-10).Date -lt $ahora.Date) { $caso.fechaVisualizacion = $ahora.ToString($fmt); $caso.fechaInicioPartida = $ahora.AddSeconds(5).ToString($fmt) }
$c = Api Post "/docente/casos" $tDoc $caso
Esperar $c 201 "crear caso (borrador)"
$idCaso = $c.Body.id
Check "estado borrador y 2 opciones" ($c.Body.estado -eq "borrador" -and @($c.Body.opciones).Count -eq 2)
$malo = $caso.Clone(); $malo.penalizacionMin = 9
Esperar (Api Post "/docente/casos" $tDoc $malo) 400 "penalización mínima > máxima -> 400"
$malo = $caso.Clone(); $malo.opciones = @()
Esperar (Api Post "/docente/casos" $tDoc $malo) 400 "caso sin opciones -> 400"
$c2 = Api Post "/docente/casos" $tDoc $caso
Esperar $c2 201 "crear un segundo caso"
Check "GET /docente/casos incluye los nuevos" (@((Api Get "/docente/casos" $tDoc).Body | Where-Object { $_.idSimulacion -eq $idSim }).Count -eq 2)
Esperar (Api Post "/docente/casos/$($c2.Body.id)/activar" $tDoc) 200 "activar el segundo caso"
$act = Api Post "/docente/casos/$idCaso/activar" $tDoc
Check "activar el primero lo deja activo" ($act.Status -eq 200 -and $act.Body.estado -eq "activo")
Check "y el segundo vuelve a borrador" ((Api Get "/docente/casos/$($c2.Body.id)" $tDoc).Body.estado -eq "borrador")
Esperar (Api Delete "/docente/casos/$($c2.Body.id)" $tDoc) 204 "eliminar el segundo caso"

# ---------------------------------------------------------------------
Write-Host "`n7. Portal del estudiante" -ForegroundColor Cyan
$tLider = Login $lider.correo "Usu-001-$($lider.cedula)!"
$tMiembro = Login $miembro.correo "Usu-001-$($miembro.cedula)!"
Check "login de los estudiantes" ($tLider -and $tMiembro)
Esperar (Api Get "/estudiante/caso-actual?idSimulacion=$idSim" $tLider) 204 "con la simulación en BORRADOR no ve caso -> 204"
Esperar (Api Post "/docente/simulaciones/$idSim/programar" $tDoc) 200 "programar la simulación"

$mis = Api Get "/estudiante/simulaciones" $tLider
$mia = @($mis.Body | Where-Object { $_.idSimulacion -eq $idSim })[0]
Check "el estudiante ve su empresa" ($mia -and $mia.nombreEmpresa -eq "Empresa Multi" -and $mia.esLider) "status $($mis.Status)"
$emp = Api Get "/estudiante/empresas/$idA" $tLider
Check "ve a sus 2 compañeros" ($emp.Status -eq 200 -and @($emp.Body.integrantes).Count -eq 2)
Esperar (Api Get "/estudiante/empresas/$idM" $tLider) 400 "empresa ajena -> 400"
Esperar (Api Get "/estudiante/simulaciones" $tDoc) 403 "token de docente en /estudiante -> 403"
Esperar (Api Get "/docente/simulaciones" $tLider) 403 "token de estudiante en /docente -> 403"

$ca = Api Get "/estudiante/caso-actual?idSimulacion=$idSim" $tLider
Check "caso actual del líder: puede decidir" ($ca.Status -eq 200 -and $ca.Body.puedeDecidir -and $null -eq $ca.Body.decision) "status $($ca.Status)"
Check "las opciones no traen resultado" ($ca.Body.caso.opciones.Count -eq 2 -and -not ($ca.Body.caso.opciones[0].PSObject.Properties.Name -contains "resultado"))
$cm = Api Get "/estudiante/caso-actual?idSimulacion=$idSim" $tMiembro
Check "el no líder no puede decidir" ($cm.Status -eq 200 -and -not $cm.Body.puedeDecidir)

$idOpcion = $ca.Body.caso.opciones[0].id
Esperar (Api Post "/estudiante/decision" $tMiembro @{ idCaso = $idCaso; idOpcion = $idOpcion }) 400 "decisión del no líder -> 400"
Esperar (Api Post "/estudiante/decision" $tLider @{ idCaso = $idCaso; idOpcion = 999999 }) 400 "opción de otro caso -> 400"
$d = Api Post "/estudiante/decision" $tLider @{ idCaso = $idCaso; idOpcion = $idOpcion }
Esperar $d 201 "el líder decide"
Check "la respuesta trae el resultado" ($d.Body.resultado -eq "Sube la capacidad 20%")
Esperar (Api Post "/estudiante/decision" $tLider @{ idCaso = $idCaso; idOpcion = $ca.Body.caso.opciones[1].id }) 400 "decidir otra vez -> 400 (permanente)"
$cm = Api Get "/estudiante/caso-actual?idSimulacion=$idSim" $tMiembro
Check "el compañero ve la decisión y su resultado" ($cm.Body.decision -and $cm.Body.decision.resultado -eq "Sube la capacidad 20%")

# ---------------------------------------------------------------------
Write-Host "`n8. El docente ve las decisiones" -ForegroundColor Cyan
$dec = Api Get "/docente/casos/$idCaso/decisiones" $tDoc
$filaA = @($dec.Body | Where-Object { $_.idEmpresa -eq $idA })[0]
$filaM = @($dec.Body | Where-Object { $_.idEmpresa -eq $idM })[0]
Check "Empresa Multi decidió 'Ampliar la planta'" ($filaA.decidio -and $filaA.opcion -eq "Ampliar la planta")
Check "Empresa Mono aún no decide" ($filaM -and -not $filaM.decidio)
Esperar (Api Put "/docente/casos/$idCaso" $tDoc $caso) 400 "editar un caso con decisiones -> 400"

# ---------------------------------------------------------------------
Write-Host "`n------------------------------------------------------------"
$color = if ($script:fallos -eq 0) { "Green" } else { "Yellow" }
Write-Host ("Resultado: {0} OK, {1} fallas   (simulación de prueba id {2})" -f $script:ok, $script:fallos, $idSim) -ForegroundColor $color
