package net.akehurst.language.editor.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object EditorIcons {

    val ORANGE = Color(255, 165, 0)

    val Error: ImageVector
        get() {
            if (_Warning != null) {
                return _Warning!!
            }
            _Warning = ImageVector.Builder(
                name = "Warning",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(40f, 840f)
                    lineToRelative(440f, -760f)
                    lineToRelative(440f, 760f)
                    close()
                    moveToRelative(138f, -80f)
                    horizontalLineToRelative(604f)
                    lineTo(480f, 240f)
                    close()
                    moveToRelative(302f, -40f)
                    quadToRelative(17f, 0f, 28.5f, -11.5f)
                    reflectiveQuadTo(520f, 680f)
                    reflectiveQuadToRelative(-11.5f, -28.5f)
                    reflectiveQuadTo(480f, 640f)
                    reflectiveQuadToRelative(-28.5f, 11.5f)
                    reflectiveQuadTo(440f, 680f)
                    reflectiveQuadToRelative(11.5f, 28.5f)
                    reflectiveQuadTo(480f, 720f)
                    moveToRelative(-40f, -120f)
                    horizontalLineToRelative(80f)
                    verticalLineToRelative(-200f)
                    horizontalLineToRelative(-80f)
                    close()
                    moveToRelative(40f, -100f)
                }
            }.build()
            return _Warning!!
        }

    val Warning: ImageVector
        get() {
            if (_Emergency_home != null) {
                return _Emergency_home!!
            }
            _Emergency_home = ImageVector.Builder(
                name = "Emergency_home",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(480f, 881f)
                    quadToRelative(-16f, 0f, -30.5f, -6f)
                    reflectiveQuadTo(423f, 858f)
                    lineTo(102f, 537f)
                    quadToRelative(-11f, -12f, -17f, -26.5f)
                    reflectiveQuadTo(79f, 480f)
                    reflectiveQuadToRelative(6f, -31f)
                    reflectiveQuadToRelative(17f, -26f)
                    lineToRelative(321f, -321f)
                    quadToRelative(12f, -12f, 26.5f, -17.5f)
                    reflectiveQuadTo(480f, 79f)
                    reflectiveQuadToRelative(31f, 5.5f)
                    reflectiveQuadToRelative(26f, 17.5f)
                    lineToRelative(321f, 321f)
                    quadToRelative(12f, 11f, 17.5f, 26f)
                    reflectiveQuadToRelative(5.5f, 31f)
                    reflectiveQuadToRelative(-5.5f, 30.5f)
                    reflectiveQuadTo(858f, 537f)
                    lineTo(537f, 858f)
                    quadToRelative(-11f, 11f, -26f, 17f)
                    reflectiveQuadToRelative(-31f, 6f)
                    moveToRelative(0f, -80f)
                    lineToRelative(321f, -321f)
                    lineToRelative(-321f, -321f)
                    lineToRelative(-321f, 321f)
                    close()
                    moveToRelative(-40f, -281f)
                    horizontalLineToRelative(80f)
                    verticalLineToRelative(-240f)
                    horizontalLineToRelative(-80f)
                    close()
                    moveToRelative(40f, 120f)
                    quadToRelative(17f, 0f, 28.5f, -11.5f)
                    reflectiveQuadTo(520f, 600f)
                    reflectiveQuadToRelative(-11.5f, -28.5f)
                    reflectiveQuadTo(480f, 560f)
                    reflectiveQuadToRelative(-28.5f, 11.5f)
                    reflectiveQuadTo(440f, 600f)
                    reflectiveQuadToRelative(11.5f, 28.5f)
                    reflectiveQuadTo(480f, 640f)
                    moveToRelative(0f, -160f)
                }
            }.build()
            return _Emergency_home!!
        }

    val Information: ImageVector
        get() {
            if (_Info != null) {
                return _Info!!
            }
            _Info = ImageVector.Builder(
                name = "Info",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(440f, 680f)
                    horizontalLineToRelative(80f)
                    verticalLineToRelative(-240f)
                    horizontalLineToRelative(-80f)
                    close()
                    moveToRelative(40f, -320f)
                    quadToRelative(17f, 0f, 28.5f, -11.5f)
                    reflectiveQuadTo(520f, 320f)
                    reflectiveQuadToRelative(-11.5f, -28.5f)
                    reflectiveQuadTo(480f, 280f)
                    reflectiveQuadToRelative(-28.5f, 11.5f)
                    reflectiveQuadTo(440f, 320f)
                    reflectiveQuadToRelative(11.5f, 28.5f)
                    reflectiveQuadTo(480f, 360f)
                    moveToRelative(0f, 520f)
                    quadToRelative(-83f, 0f, -156f, -31.5f)
                    reflectiveQuadTo(197f, 763f)
                    reflectiveQuadToRelative(-85.5f, -127f)
                    reflectiveQuadTo(80f, 480f)
                    reflectiveQuadToRelative(31.5f, -156f)
                    reflectiveQuadTo(197f, 197f)
                    reflectiveQuadToRelative(127f, -85.5f)
                    reflectiveQuadTo(480f, 80f)
                    reflectiveQuadToRelative(156f, 31.5f)
                    reflectiveQuadTo(763f, 197f)
                    reflectiveQuadToRelative(85.5f, 127f)
                    reflectiveQuadTo(880f, 480f)
                    reflectiveQuadToRelative(-31.5f, 156f)
                    reflectiveQuadTo(763f, 763f)
                    reflectiveQuadToRelative(-127f, 85.5f)
                    reflectiveQuadTo(480f, 880f)
                    moveToRelative(0f, -80f)
                    quadToRelative(134f, 0f, 227f, -93f)
                    reflectiveQuadToRelative(93f, -227f)
                    reflectiveQuadToRelative(-93f, -227f)
                    reflectiveQuadToRelative(-227f, -93f)
                    reflectiveQuadToRelative(-227f, 93f)
                    reflectiveQuadToRelative(-93f, 227f)
                    reflectiveQuadToRelative(93f, 227f)
                    reflectiveQuadToRelative(227f, 93f)
                    moveToRelative(0f, -320f)
                }
            }.build()
            return _Info!!
        }

    private var _Warning: ImageVector? = null
    private var _Emergency_home: ImageVector? = null
    private var _Info: ImageVector? = null

}