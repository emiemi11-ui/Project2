package com.vitanova.app.ui.fitness

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.ui.theme.FitnessAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaCyan
import com.vitanova.app.ui.theme.VitaError
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning
import kotlinx.coroutines.delay
import java.util.Locale

// ── Data Models ──────────────────────────────────────────────────────────────

private data class BodyZone(
    val name: String,
    val emoji: String,
    val color: Color,
    val stretches: List<Stretch>
)

private data class Stretch(
    val name: String,
    val description: String,
    val defaultDurationSec: Int = 30
)

private data class StretchRoutine(
    val name: String,
    val durationMinutes: Int,
    val description: String,
    val color: Color,
    val stretches: List<RoutineStretch>
)

private data class RoutineStretch(
    val name: String,
    val description: String,
    val durationSec: Int
)

// ── Stretching Database ──────────────────────────────────────────────────────

private val bodyZones = listOf(
    BodyZone(
        name = "Cap/Gat",
        emoji = "\uD83D\uDDE3",
        color = Color(0xFFEF4444),
        stretches = listOf(
            Stretch("Flexie cervicala laterala", "Inclina urechea spre umar, fara a ridica umarul opus. Intinderea musculaturii sternocleidomastoidian si scalenilor laterali. Mentine scapulele retrase.", 30),
            Stretch("Rotatie cervicala", "Roteste capul incet spre dreapta, tine 10s, apoi stanga. Mobilizeaza articulatiile atlanto-axiale si atlantooccipitale. Evita hiperextensia.", 30),
            Stretch("Flexie cervicala anterioara", "Brarbia spre piept, mainile pe ceafa cu presiune lejera. Intinde trapezul superior si musculatura suboccipitala. Respira profund.", 30),
            Stretch("Extensie cervicala", "Capul usor pe spate, priveste tavanul. Intinde muschii flexori cervicali: sternocleidomastoidian, scaleni. Nu forta.", 30)
        )
    ),
    BodyZone(
        name = "Umeri",
        emoji = "\uD83D\uDCAA",
        color = Color(0xFFF97316),
        stretches = listOf(
            Stretch("Cross-body shoulder stretch", "Bratul drept intins orizontal peste piept, trage cu mana stanga de cot. Intinde deltoidul posterior si capsula articulara posterioara.", 30),
            Stretch("Overhead triceps stretch", "Bratul deasupra capului, cotul indoit, mana cealalta trage cotul. Intinde tricepsul lung si dorsalul mare prin origine.", 30),
            Stretch("Doorway chest stretch", "Antebratele pe tocul usii, pas inainte. Intinde pectoralul mare fasciculul clavicular si deltoidul anterior.", 30),
            Stretch("Behind-back clasp", "Mainile la spate, degete impreunate, ridica bratele. Intinde pectoralii, deltoidul anterior si bicepsul.", 30),
            Stretch("Wall angel", "Spatele si bratele lipite de perete, aluneca bratele sus-jos. Mobilizeaza articulatia scapulotoracica.", 30)
        )
    ),
    BodyZone(
        name = "Spate",
        emoji = "\uD83E\uDDBB",
        color = Color(0xFF3B82F6),
        stretches = listOf(
            Stretch("Cat-cow stretch", "Pe maini si genunchi, alterneaza arcuirea si rotunjirea spatelui. Mobilizeaza coloana vertebrala segment cu segment, de la cervical la lombar.", 30),
            Stretch("Child's pose", "Genunchi departe, brate intinse, fruntea pe sol. Intinde erectorul spinal, dorsalul mare si fascia toracolombara.", 45),
            Stretch("Seated spinal twist", "Sezand, un picior intins, celalalt peste, roteste trunchiul. Intinde oblicii, piriformisul si erectorul spinal controlateral.", 30),
            Stretch("Cobra stretch", "Intins pe burta, ridica pieptul cu bratele. Extensie lombara, intinde rectul abdominal si fascia abdominala.", 30),
            Stretch("Knee-to-chest", "Pe spate, trage genunchiul spre piept. Intinde erectorul spinal lombar, gluteul mare si capsula articulara posterioara a soldului.", 30)
        )
    ),
    BodyZone(
        name = "Piept",
        emoji = "\uD83D\uDC9A",
        color = Color(0xFF10B981),
        stretches = listOf(
            Stretch("Doorway pec stretch", "Bratul pe tocul usii la 90 grade, pas inainte. Intinde pectoralul mare, fasciculul sternocostal si cel clavicular.", 30),
            Stretch("Floor pec stretch", "Intins pe burta, bratul intins lateral la 90 grade, ruleaza pe cealalta parte. Intindere profunda a pectoralului.", 30),
            Stretch("Clasped hands behind", "Maini impreunate la spate, ridica bratele, piept in fata. Retractie scapulara cu intinderea pectoralilor si deltoidului anterior.", 30),
            Stretch("Wall corner stretch", "Ambele brate pe pereti intr-un colt, pas inainte. Intindere bilaterala a pectoralilor si a deltoidului anterior.", 30)
        )
    ),
    BodyZone(
        name = "Brate",
        emoji = "\uD83D\uDCAA",
        color = Color(0xFF8B5CF6),
        stretches = listOf(
            Stretch("Wrist flexor stretch", "Bratul intins, palma in sus, trage degetele in jos cu cealalta mana. Intinde flexorii antebratului: flexorul radial si ulnar al carpului.", 30),
            Stretch("Wrist extensor stretch", "Bratul intins, palma in jos, trage dosul palmii in jos. Intinde extensorii antebratului si musculatura laterala.", 30),
            Stretch("Biceps wall stretch", "Palma pe perete la nivelul umarului, roteste corpul opus. Intinde bicepsul brahial si brahialul anterior.", 30),
            Stretch("Triceps overhead stretch", "Bratul deasupra capului, cotul indoit la maxim, presiune pe cot. Intinde capul lung al tricepsului.", 30),
            Stretch("Prayer stretch", "Palme impreunate la piept, coboari mainile mentinand contactul. Intinde flexorii si pronatorii.", 30)
        )
    ),
    BodyZone(
        name = "Abdomen",
        emoji = "\uD83C\uDFAF",
        color = Color(0xFFEAB308),
        stretches = listOf(
            Stretch("Cobra stretch", "Intins pe burta, ridica pieptul cu bratele drepte. Extensie lombara, intinde rectul abdominal pe toata lungimea.", 30),
            Stretch("Seated side bend", "Sezand picior peste picior, inclina lateral cu bratul intins. Intinde oblicul extern si intern, patrat lombar.", 30),
            Stretch("Standing back extension", "In picioare, maini pe solduri, inclina-te usor pe spate. Intinde rectul abdominal si fascia abdominala.", 30),
            Stretch("Supine twist", "Pe spate, genunchii la piept, lasa-i sa cada lateral. Intinde oblicii, transversul abdominal si erectorul controlateral.", 45)
        )
    ),
    BodyZone(
        name = "Solduri",
        emoji = "\uD83E\uDDBF",
        color = Color(0xFFEC4899),
        stretches = listOf(
            Stretch("Pigeon pose", "Un picior indoit in fata, celalalt intins in spate. Intinde piriformisul, gluteul mediu si capsula anterioara a soldului opus.", 45),
            Stretch("Hip flexor lunge", "Genunchi pe sol, pas mare in fata, impinge soldul inainte. Intinde iliopsoasul, rectul femural si tensorul fasciei lata.", 30),
            Stretch("Butterfly stretch", "Sezand, talpi impreunate, genunchi in lateral. Intinde aductorii: aductorul lung, scurt si mare, gracilisul.", 30),
            Stretch("90-90 hip stretch", "Ambele picioare la 90 grade pe sol, alterneza pozitia. Mobilizeaza rotatia interna si externa a soldului.", 30),
            Stretch("Figure 4 stretch", "Pe spate, glezna pe genunchiul opus, trage spre piept. Intinde piriformisul si rotatorii externi ai soldului.", 30)
        )
    ),
    BodyZone(
        name = "Picioare",
        emoji = "\uD83E\uDDB5",
        color = Color(0xFF06B6D4),
        stretches = listOf(
            Stretch("Standing quad stretch", "In picioare, trage calcaiul spre fesier. Intinde rectul femural si vastul intermediar. Mentine genunchii apropiati.", 30),
            Stretch("Standing hamstring stretch", "Piciorul pe o suprafata, inclina trunchiul inainte. Intinde bicepsul femural, semimembranosul si semitendinosul.", 30),
            Stretch("Standing calf stretch", "Maini pe perete, un picior in spate cu calcaiul pe sol. Intinde gastrocnemianul si soleanul.", 30),
            Stretch("Sumo squat stretch", "Genuflexiune larga, coatele impinge genunchii in afara. Intinde aductorii si deschide soldurile.", 30),
            Stretch("IT band stretch", "In picioare, picior peste picior, inclina-te lateral. Intinde banda iliotibala si tensorul fasciei lata.", 30),
            Stretch("Seated forward fold", "Sezand, picioare intinse, aplica-te inainte. Intinde ischiogambierii si erectorul spinal lombar.", 45)
        )
    ),
    BodyZone(
        name = "Glezne/Picioare",
        emoji = "\uD83E\uDDB6",
        color = Color(0xFF14B8A6),
        stretches = listOf(
            Stretch("Ankle circles", "Roteste glezna in cerc complet, ambele directii. Mobilizeaza articulatia talocrurala si subtalara.", 30),
            Stretch("Toe pulls", "Sezand, trage degetele piciorului spre tine. Intinde fascia plantara si flexorii scurti ai degetelor.", 30),
            Stretch("Achilles stretch", "Pe treapta, coboari calcaiul sub nivel. Intinde tendonul ahilean, soleanul si gastrocnemianul.", 30),
            Stretch("Plantar fascia roll", "Ruleaza o minge sub talpa. Elibereaza fascia plantara si musculatura intrinseca a piciorului.", 45)
        )
    )
)

// ── Pre-built Routines ───────────────────────────────────────────────────────

private val stretchRoutines = listOf(
    StretchRoutine(
        name = "Post-antrenament",
        durationMinutes = 15,
        description = "Recuperare completa dupa antrenament intens",
        color = FitnessAccent,
        stretches = listOf(
            RoutineStretch("Standing quad stretch", "Trage calcaiul spre fesier, mentine 30s pe fiecare picior.", 60),
            RoutineStretch("Standing hamstring stretch", "Piciorul pe suprafata, inclina-te inainte. 30s pe fiecare picior.", 60),
            RoutineStretch("Hip flexor lunge", "Pas mare, genunchi pe sol, impinge soldul. 30s per parte.", 60),
            RoutineStretch("Pigeon pose", "Picior indoit in fata, intinde soldul posterior. 45s per parte.", 90),
            RoutineStretch("Cross-body shoulder stretch", "Bratul peste piept, trage de cot. 30s per parte.", 60),
            RoutineStretch("Standing calf stretch", "Maini pe perete, calcai pe sol. 30s per picior.", 60),
            RoutineStretch("Cobra stretch", "Intins pe burta, ridica pieptul. Extensie lombara.", 45),
            RoutineStretch("Child's pose", "Genunchi departe, brate intinse, relaxeaza spatele.", 60),
            RoutineStretch("Seated spinal twist", "Rotatie controlata a coloanei. 30s per parte.", 60),
            RoutineStretch("Supine twist", "Pe spate, genunchi lateral. Relaxeaza oblicii. 30s per parte.", 60),
            RoutineStretch("Butterfly stretch", "Talpi impreunate, genunchi lateral. Deschide soldurile.", 45),
            RoutineStretch("Seated forward fold", "Picioare intinse, aplica-te inainte. Ischiogambieri.", 60)
        )
    ),
    StretchRoutine(
        name = "Birou",
        durationMinutes = 10,
        description = "Elibereaza tensiunea de la birou si calculator",
        color = Color(0xFF3B82F6),
        stretches = listOf(
            RoutineStretch("Flexie cervicala laterala", "Urechea spre umar, 20s per parte. Elibereaza gatul.", 40),
            RoutineStretch("Rotatie cervicala", "Roteste capul incet spre fiecare parte, 15s.", 30),
            RoutineStretch("Doorway chest stretch", "Brate pe tocul usii, pas inainte. Deschide pieptul.", 45),
            RoutineStretch("Cross-body shoulder stretch", "Bratul peste piept. 20s per parte.", 40),
            RoutineStretch("Wrist flexor stretch", "Bratul intins, trage degetele. 15s per mana.", 30),
            RoutineStretch("Wrist extensor stretch", "Bratul intins, trage dosul mainii. 15s per mana.", 30),
            RoutineStretch("Seated spinal twist", "Rotatie pe scaun. 20s per parte.", 40),
            RoutineStretch("Seated side bend", "Inclina lateral cu bratul intins. 20s per parte.", 40),
            RoutineStretch("Standing quad stretch", "In picioare, trage calcaiul. 20s per picior.", 40),
            RoutineStretch("Standing hamstring stretch", "Piciorul pe scaun, inclina-te. 20s per picior.", 40),
            RoutineStretch("Cat-cow stretch", "Alterneza arcuirea spatelui pe scaun.", 45),
            RoutineStretch("Standing back extension", "Maini pe solduri, extensie usoara.", 30)
        )
    ),
    StretchRoutine(
        name = "Dimineata",
        durationMinutes = 10,
        description = "Trezeste corpul si pregateste-te pentru zi",
        color = VitaWarning,
        stretches = listOf(
            RoutineStretch("Cat-cow stretch", "Pe maini si genunchi, mobilizeaza coloana.", 45),
            RoutineStretch("Child's pose", "Intinde spatele si relaxeaza umerii.", 45),
            RoutineStretch("Cobra stretch", "Extensie usoara, deschide pieptul.", 30),
            RoutineStretch("Downward dog", "V inversat, intinde ischiogambierii si spatele.", 45),
            RoutineStretch("Hip flexor lunge", "Deschide soldurile dupa noapte. 30s per parte.", 60),
            RoutineStretch("Standing quad stretch", "Activeaza cvadricepsii. 20s per picior.", 40),
            RoutineStretch("Standing hamstring stretch", "Intinde posterior. 20s per picior.", 40),
            RoutineStretch("Cross-body shoulder stretch", "Pregateste umerii. 15s per parte.", 30),
            RoutineStretch("Flexie cervicala laterala", "Elibereaza gatul. 15s per parte.", 30),
            RoutineStretch("Standing side bend", "Intinde lateralele. 15s per parte.", 30),
            RoutineStretch("Ankle circles", "Mobilizeaza gleznele. Ambele directii.", 30),
            RoutineStretch("Full body stretch", "Intinde-te complet pe varfuri, brate sus.", 30)
        )
    )
)

// ── Stretching Screen ────────────────────────────────────────────────────────

@Composable
fun StretchingScreen(
    onNavigateBack: () -> Unit
) {
    var selectedZone by remember { mutableStateOf<BodyZone?>(null) }
    var selectedStretch by remember { mutableStateOf<Stretch?>(null) }
    var selectedTimerDuration by remember { mutableIntStateOf(30) }
    var showStretchTimer by remember { mutableStateOf(false) }
    var activeRoutine by remember { mutableStateOf<StretchRoutine?>(null) }
    var routineIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                when {
                    showStretchTimer -> {
                        showStretchTimer = false
                        selectedStretch = null
                    }
                    activeRoutine != null -> {
                        activeRoutine = null
                        routineIndex = 0
                    }
                    selectedZone != null -> selectedZone = null
                    else -> onNavigateBack()
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Inapoi",
                    tint = VitaTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.SelfImprovement,
                contentDescription = null,
                tint = VitaCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    activeRoutine != null -> activeRoutine!!.name
                    selectedZone != null -> selectedZone!!.name
                    else -> "Stretching"
                },
                style = MaterialTheme.typography.titleLarge,
                color = VitaTextPrimary
            )
        }

        when {
            activeRoutine != null -> {
                RoutinePlayerView(
                    routine = activeRoutine!!,
                    currentIndex = routineIndex,
                    onIndexChange = { routineIndex = it },
                    onFinish = {
                        activeRoutine = null
                        routineIndex = 0
                    }
                )
            }
            showStretchTimer && selectedStretch != null -> {
                SingleStretchTimerView(
                    stretch = selectedStretch!!,
                    durationSec = selectedTimerDuration,
                    accentColor = selectedZone?.color ?: VitaCyan,
                    onClose = {
                        showStretchTimer = false
                        selectedStretch = null
                    }
                )
            }
            selectedZone != null -> {
                StretchListView(
                    zone = selectedZone!!,
                    onSelectStretch = { stretch, duration ->
                        selectedStretch = stretch
                        selectedTimerDuration = duration
                        showStretchTimer = true
                    }
                )
            }
            else -> {
                MainStretchingView(
                    onSelectZone = { selectedZone = it },
                    onStartRoutine = { routine ->
                        activeRoutine = routine
                        routineIndex = 0
                    }
                )
            }
        }
    }
}

// ── Main View ────────────────────────────────────────────────────────────────

@Composable
private fun MainStretchingView(
    onSelectZone: (BodyZone) -> Unit,
    onStartRoutine: (StretchRoutine) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pre-built routines
        item {
            Text(
                text = "Rutine rapide",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary
            )
        }

        items(stretchRoutines) { routine ->
            RoutineCard(routine = routine, onClick = { onStartRoutine(routine) })
        }

        // Body zone grid
        item {
            Text(
                text = "Zone corporale",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            BodyZoneGrid(
                zones = bodyZones,
                onSelectZone = onSelectZone
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Routine Card ─────────────────────────────────────────────────────────────

@Composable
private fun RoutineCard(
    routine: StretchRoutine,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            routine.color.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = routine.color.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SelfImprovement,
                        contentDescription = null,
                        tint = routine.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = VitaTextPrimary
                    )
                    Text(
                        text = routine.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = VitaTextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${routine.durationMinutes} min | ${routine.stretches.size} exercitii",
                            style = MaterialTheme.typography.labelSmall,
                            color = VitaTextTertiary
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = routine.color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ── Body Zone Grid ───────────────────────────────────────────────────────────

@Composable
private fun BodyZoneGrid(
    zones: List<BodyZone>,
    onSelectZone: (BodyZone) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        userScrollEnabled = false
    ) {
        items(zones) { zone ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onSelectZone(zone) },
                colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    zone.color.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = zone.color.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SelfImprovement,
                                contentDescription = null,
                                tint = zone.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = VitaTextPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${zone.stretches.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = VitaTextTertiary
                        )
                    }
                }
            }
        }
    }
}

// ── Stretch List for a Zone ──────────────────────────────────────────────────

@Composable
private fun StretchListView(
    zone: BodyZone,
    onSelectStretch: (Stretch, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${zone.stretches.size} intinderi pentru ${zone.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = VitaTextSecondary
            )
        }

        items(zone.stretches) { stretch ->
            StretchCard(
                stretch = stretch,
                accentColor = zone.color,
                onStart = { duration -> onSelectStretch(stretch, duration) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun StretchCard(
    stretch: Stretch,
    accentColor: Color,
    onStart: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SelfImprovement,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stretch.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = VitaTextPrimary
                    )
                    Text(
                        text = "${stretch.defaultDurationSec}s recomandat",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stretch.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Timer duration options
                Text(
                    text = "Durata timer:",
                    style = MaterialTheme.typography.labelMedium,
                    color = VitaTextTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30, 45, 60).forEach { duration ->
                        FilledTonalButton(
                            onClick = { onStart(duration) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = accentColor.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${duration}s",
                                style = MaterialTheme.typography.labelLarge,
                                color = accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Single Stretch Timer ─────────────────────────────────────────────────────

@Composable
private fun SingleStretchTimerView(
    stretch: Stretch,
    durationSec: Int,
    accentColor: Color,
    onClose: () -> Unit
) {
    var timeRemainingMs by remember { mutableStateOf((durationSec * 1000).toLong()) }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning && !isFinished) {
            while (isRunning && timeRemainingMs > 0) {
                delay(100)
                timeRemainingMs -= 100
            }
            if (timeRemainingMs <= 0) {
                isFinished = true
                isRunning = false
            }
        }
    }

    val totalDurationMs = durationSec * 1000L
    val progress = if (totalDurationMs > 0) {
        1f - (timeRemainingMs.toFloat() / totalDurationMs)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(100, easing = LinearEasing),
        label = "stretch_timer_progress"
    )

    val timeSeconds = (timeRemainingMs / 1000).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stretch.name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = VitaTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stretch.description,
            style = MaterialTheme.typography.bodyMedium,
            color = VitaTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.weight(0.3f))

        // Timer ring
        Box(
            modifier = Modifier
                .size(240.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = VitaSurfaceVariant
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2
                )
                val arcSize = Size(radius * 2, radius * 2)

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val ringColor = if (isFinished) VitaGreen else accentColor
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isFinished) {
                    Text(
                        text = "Gata!",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = VitaGreen
                    )
                } else {
                    Text(
                        text = "$timeSeconds",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 72.sp
                        ),
                        color = VitaTextPrimary
                    )
                    Text(
                        text = "secunde",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // Controls
        if (isFinished) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        timeRemainingMs = (durationSec * 1000).toLong()
                        isFinished = false
                        isRunning = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = accentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Repeta",
                        color = accentColor
                    )
                }
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VitaGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Gata",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        } else {
            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) VitaSurfaceElevated else accentColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = if (isRunning) VitaTextPrimary else Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "PAUZA" else "START",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = if (isRunning) VitaTextPrimary else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── Routine Player ───────────────────────────────────────────────────────────

@Composable
private fun RoutinePlayerView(
    routine: StretchRoutine,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onFinish: () -> Unit
) {
    val currentStretch = routine.stretches.getOrNull(currentIndex)
    val totalStretches = routine.stretches.size
    val isLastStretch = currentIndex >= totalStretches - 1

    var timeRemainingMs by remember(currentIndex) {
        mutableStateOf(((currentStretch?.durationSec ?: 30) * 1000).toLong())
    }
    var isRunning by remember { mutableStateOf(false) }
    var isStretchFinished by remember(currentIndex) { mutableStateOf(false) }

    LaunchedEffect(isRunning, currentIndex) {
        if (isRunning && !isStretchFinished) {
            while (isRunning && timeRemainingMs > 0) {
                delay(100)
                timeRemainingMs -= 100
            }
            if (timeRemainingMs <= 0) {
                isStretchFinished = true
                isRunning = false
            }
        }
    }

    val totalDurationMs = ((currentStretch?.durationSec ?: 30) * 1000).toLong()
    val progress = if (totalDurationMs > 0) {
        1f - (timeRemainingMs.toFloat() / totalDurationMs)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(100, easing = LinearEasing),
        label = "routine_timer_progress"
    )

    val timeSeconds = (timeRemainingMs / 1000).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress indicator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Exercitiu ${currentIndex + 1}/$totalStretches",
                            style = MaterialTheme.typography.labelMedium,
                            color = routine.color
                        )
                        Text(
                            text = "${routine.durationMinutes} min total",
                            style = MaterialTheme.typography.labelSmall,
                            color = VitaTextTertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(VitaSurfaceVariant, RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((currentIndex + 1).toFloat() / totalStretches)
                                .height(4.dp)
                                .background(routine.color, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        if (currentStretch != null) {
            // Stretch name and description
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentStretch.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = VitaTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentStretch.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Timer
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = VitaSurfaceVariant
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 12.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val topLeft = Offset(
                            (size.width - radius * 2) / 2,
                            (size.height - radius * 2) / 2
                        )
                        val arcSize = Size(radius * 2, radius * 2)

                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        val ringColor = if (isStretchFinished) VitaGreen else routine.color
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isStretchFinished) {
                            Text(
                                text = "Gata!",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = VitaGreen
                            )
                        } else {
                            Text(
                                text = "$timeSeconds",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 64.sp
                                ),
                                color = VitaTextPrimary
                            )
                            Text(
                                text = "secunde",
                                style = MaterialTheme.typography.bodySmall,
                                color = VitaTextTertiary
                            )
                        }
                    }
                }
            }

            // Controls
            item {
                if (isStretchFinished) {
                    Button(
                        onClick = {
                            if (isLastStretch) {
                                onFinish()
                            } else {
                                onIndexChange(currentIndex + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastStretch) VitaGreen else routine.color
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isLastStretch) Icons.Filled.SelfImprovement else Icons.Filled.SkipNext,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLastStretch) "RUTINA COMPLETA!" else "URMATORUL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { isRunning = !isRunning },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRunning) VitaSurfaceElevated else routine.color
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = if (isRunning) VitaTextPrimary else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRunning) "PAUZA" else "START",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isRunning) VitaTextPrimary else Color.White
                            )
                        }

                        if (!isLastStretch) {
                            FilledTonalButton(
                                onClick = { onIndexChange(currentIndex + 1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = VitaSurfaceElevated
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = null,
                                    tint = VitaTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SKIP",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = VitaTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Upcoming stretches
            if (currentIndex < totalStretches - 1) {
                item {
                    Text(
                        text = "Urmatoarele",
                        style = MaterialTheme.typography.titleSmall,
                        color = VitaTextTertiary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                val upcoming = routine.stretches.drop(currentIndex + 1).take(3)
                items(upcoming) { upcomingStretch ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NavigateNext,
                                contentDescription = null,
                                tint = VitaTextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = upcomingStretch.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VitaTextSecondary
                                )
                            }
                            Text(
                                text = "${upcomingStretch.durationSec}s",
                                style = MaterialTheme.typography.labelMedium,
                                color = VitaTextTertiary
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}
