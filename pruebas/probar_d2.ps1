# =====================================================================
#  Prueba D2: arrastre del balance entre dos casos, cierre de casos y clasificación encadenada
#  Uso (PowerShell, con el backend corriendo y la migración 2026-10-09 aplicada):
#     powershell -ExecutionPolicy Bypass -File .\pruebas\probar_d2.ps1
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
Write-Host "`nPrueba D2 contra $Base`n" -ForegroundColor Cyan

# ---------------------------------------------------------------------
Write-Host "1. Preparación: simulación, 2 empresas y 2 casos" -ForegroundColor Cyan
$tDoc = Login $Docente $ClaveDocente
$sim = Api Post "/docente/simulaciones" $tDoc @{ nombre = "Prueba D2 $($ahora.ToString('yyyy-MM-dd HH:mm'))"; fechaInicio = $ahora.ToString("yyyy-MM-dd"); fechaFin = $ahora.AddDays(30).ToString("yyyy-MM-dd") }
Esperar $sim 201 "crear simulación"
$idSim = $sim.Body.id
$idA = (Api Post "/docente/simulaciones/$idSim/empresas" $tDoc @{ nombre = "Empresa Multi"; tipoJugador = "MULTIUSUARIO" }).Body.id
$idM = (Api Post "/docente/simulaciones/$idSim/empresas" $tDoc @{ nombre = "Empresa Mono"; tipoJugador = "MONOUSUARIO" }).Body.id
Esperar (Api Post "/docente/empresas/$idA/integrantes" $tDoc @{ idUsuario = $lider.id; departamento = "COMERCIAL"; esLider = $true }) 201 "líder de Empresa Multi"
Esperar (Api Post "/docente/empresas/$idM/integrantes" $tDoc @{ idUsuario = $mono.id }) 201 "integrante (líder) de Empresa Mono"

# Caso 1: balance del docente (activo 1.000.000; utilidad 100.000), penalización máx. 8 %
$caso1 = @{
    idSimulacion = $idSim; nombre = "Caso 1 - TextilAndes"; tipo = "Manufactura"; mision = "M"; vision = "V"
    financiero = @{
        efectivo = 200000; cuentasPorCobrar = 150000; inventarios = 250000; propiedadPlantaEquipo = 350000; activosIntangibles = 50000
        cuentasPorPagar = 150000; obligacionesFinancierasCortoPlazo = 100000; obligacionesFinancierasLargoPlazo = 150000
        capitalSocial = 400000; utilidadesRetenidas = 100000
        ventasNetas = 800000; costoVentas = 500000; gastosAdministracion = 80000; gastosVentas = 60000; gastosFinancieros = 20000; impuestoRenta = 40000
        flujoOperativo = 120000; flujoInversion = -80000; flujoFinanciacion = -20000 }
    penalizacionMin = 2; penalizacionMax = 8
    fechaVisualizacion = $ahora.AddMinutes(-30).ToString($fmt)
    fechaInicioPartida = $ahora.AddMinutes(-10).ToString($fmt)
    fechaFinPartida    = $ahora.AddHours(2).ToString($fmt)
    opciones = @(@{ opcion = "Ampliar la planta"; resultado = "Ventas +15 %, gastos financieros +30.000"
                    impacto = @{ ventasNetas = @{ tipo = "porcentaje"; valor = 15 }; gastosFinancieros = @{ tipo = "monto"; valor = 30000 } } },
                 @{ opcion = "Esperar"; resultado = "Nada cambia" })
}
# Caso 2: solo estado de resultados (utilidad 120.000), penalización máx. 10 %
$caso2 = @{
    idSimulacion = $idSim; nombre = "Caso 2 - TextilAndes"; tipo = "Manufactura"; mision = "M"; vision = "V"
    financiero = @{ ventasNetas = 900000; costoVentas = 550000; gastosAdministracion = 90000; gastosVentas = 70000; gastosFinancieros = 20000; impuestoRenta = 50000 }
    penalizacionMin = 2; penalizacionMax = 10
    fechaVisualizacion = $ahora.AddMinutes(-30).ToString($fmt)
    fechaInicioPartida = $ahora.AddMinutes(-5).ToString($fmt)
    fechaFinPartida    = $ahora.AddHours(3).ToString($fmt)
    opciones = @(@{ opcion = "Expandir"; resultado = "Ventas +10 %"; impacto = @{ ventasNetas = @{ tipo = "porcentaje"; valor = 10 } } },
                 @{ opcion = "Mantener"; resultado = "Nada cambia" })
}
$c1 = Api Post "/docente/casos" $tDoc $caso1
Esperar $c1 201 "crear caso 1 (con balance)"
$c2 = Api Post "/docente/casos" $tDoc $caso2
Esperar $c2 201 "crear caso 2 (solo estado de resultados)"
Check "caso 1 es el primero y caso 2 no" ($c1.Body.esPrimerCaso -eq $true -and $c2.Body.esPrimerCaso -eq $false)
$idC1 = $c1.Body.id; $idC2 = $c2.Body.id

# ---------------------------------------------------------------------
Write-Host "`n2. Se juega el caso 1" -ForegroundColor Cyan
Esperar (Api Post "/docente/simulaciones/$idSim/programar" $tDoc) 200 "programar la simulación"
Esperar (Api Post "/docente/simulaciones/$idSim/iniciar" $tDoc) 200 "iniciar la simulación"
Esperar (Api Post "/docente/casos/$idC1/activar" $tDoc) 200 "activar el caso 1"
$tLider = Login $lider.correo "Usu-001-$($lider.cedula)!"
$tMono  = Login $mono.correo  "Usu-001-$($mono.cedula)!"
Esperar (Api Post "/estudiante/decision" $tLider @{ idCaso = $idC1; idOpcion = $c1.Body.opciones[0].id }) 201 "Multi elige 'Ampliar la planta' (Mono no decide)"
$cl = Api Get "/docente/simulaciones/$idSim/clasificacion" $tDoc
Check "con el caso 1 abierto todavía no cuenta en la clasificación" ($cl.Body.casosConsiderados -eq 0) "casos $($cl.Body.casosConsiderados)"

# ---------------------------------------------------------------------
Write-Host "`n3. Activar el caso 2 cierra el caso 1" -ForegroundColor Cyan
Esperar (Api Post "/docente/casos/$idC2/activar" $tDoc) 200 "activar el caso 2 (antes de que termine el caso 1)"
$cl = Api Get "/docente/simulaciones/$idSim/clasificacion" $tDoc
$filas = @($cl.Body.clasificacion)
$multi = @($filas | Where-Object { $_.idEmpresa -eq $idA })[0]
$monoF = @($filas | Where-Object { $_.idEmpresa -eq $idM })[0]
Check "ahora cuenta 1 caso" ($cl.Body.casosConsiderados -eq 1) "casos $($cl.Body.casosConsiderados)"
# Multi: ventas 920.000 − 500.000 − 80.000 − 60.000 − 50.000 − 40.000 = 190.000
Check "Multi 190.000 en el caso 1 (decisión)" ($multi.utilidadAcumulada -eq 190000) "Multi $($multi.utilidadAcumulada)"
# Mono: 100.000 − 8 % = 92.000
Check "Mono 92.000 en el caso 1 (penalizada 8 %)" ($monoF.utilidadAcumulada -eq 92000 -and $monoF.casosSinDecision -eq 1) "Mono $($monoF.utilidadAcumulada)"

# ---------------------------------------------------------------------
Write-Host "`n4. Se juega el caso 2 y se finaliza" -ForegroundColor Cyan
Esperar (Api Post "/estudiante/decision" $tMono @{ idCaso = $idC2; idOpcion = $c2.Body.opciones[0].id }) 201 "Mono elige 'Expandir' (Multi no decide)"
Esperar (Api Post "/docente/simulaciones/$idSim/finalizar" $tDoc) 200 "finalizar la simulación (cierra el caso 2)"
$cl = Api Get "/docente/simulaciones/$idSim/clasificacion" $tDoc
$filas = @($cl.Body.clasificacion)
Check "definitiva con 2 casos" ($cl.Body.definitiva -and $cl.Body.casosConsiderados -eq 2) "casos $($cl.Body.casosConsiderados)"
# Mono: 92.000 + (990.000 − 550.000 − 90.000 − 70.000 − 20.000 − 50.000 = 210.000) = 302.000
# Multi: 190.000 + (120.000 − 10 % = 108.000) = 298.000
Check "1.º Empresa Mono con 302.000" ($filas[0].idEmpresa -eq $idM -and $filas[0].utilidadAcumulada -eq 302000) "$($filas[0].nombreEmpresa) $($filas[0].utilidadAcumulada)"
Check "2.º Empresa Multi con 298.000" ($filas[1].idEmpresa -eq $idA -and $filas[1].utilidadAcumulada -eq 298000) "$($filas[1].nombreEmpresa) $($filas[1].utilidadAcumulada)"
Check "el desglose de Multi: decidió el caso 1 y fue penalizada en el caso 2" ($filas[1].desglose[0].decidio -and -not $filas[1].desglose[1].decidio -and $filas[1].desglose[1].penalizacionPorcentaje -eq 10)

# ---------------------------------------------------------------------
Write-Host "`n------------------------------------------------------------"
$color = if ($script:fallos -eq 0) { "Green" } else { "Yellow" }
Write-Host ("Resultado: {0} OK, {1} fallas   (simulación de prueba id {2})" -f $script:ok, $script:fallos, $idSim) -ForegroundColor $color
Write-Host "`nPara ver el arrastre del balance en Neon:" -ForegroundColor Cyan
Write-Host @"
SELECT e.nombre AS empresa, c.nombre_empresa AS caso, f.momento, f.origen,
       f.efectivo, f.utilidades_retenidas,
       f.ventas_netas - f.costo_ventas - f.gastos_administracion - f.gastos_ventas - f.gastos_financieros - f.impuesto_renta AS utilidad_neta
FROM estado_financiero_empresa f
JOIN empresa e ON e.id_empresa = f.id_empresa
JOIN caso c ON c.id_caso = f.id_caso
WHERE c.id_simulacion = $idSim
ORDER BY e.nombre, c.fecha_inicio, f.momento DESC;
"@
Write-Host @"
Esperado: Multi INICIAL caso 2 = HEREDADO con utilidades_retenidas 290000 (100.000 + 190.000);
          Mono  INICIAL caso 2 = HEREDADO con utilidades_retenidas 192000 (100.000 + 92.000).
"@
