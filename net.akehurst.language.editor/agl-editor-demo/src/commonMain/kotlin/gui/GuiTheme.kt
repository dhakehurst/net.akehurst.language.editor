package net.akehurst.language.editor.demo.gui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

class GuiTheme {

    // Icon code from https://composeicons.com/
    object Icons {
        private var _FOLDER: ImageVector? = null
        val FOLDER: ImageVector
            get() {
                if (_FOLDER != null) {
                    return _FOLDER!!
                }
                _FOLDER = ImageVector.Builder(
                    name = "Folder",
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
                        moveTo(160f, 800f)
                        quadToRelative(-33f, 0f, -56.5f, -23.5f)
                        reflectiveQuadTo(80f, 720f)
                        verticalLineToRelative(-480f)
                        quadToRelative(0f, -33f, 23.5f, -56.5f)
                        reflectiveQuadTo(160f, 160f)
                        horizontalLineToRelative(240f)
                        lineToRelative(80f, 80f)
                        horizontalLineToRelative(320f)
                        quadToRelative(33f, 0f, 56.5f, 23.5f)
                        reflectiveQuadTo(880f, 320f)
                        verticalLineToRelative(400f)
                        quadToRelative(0f, 33f, -23.5f, 56.5f)
                        reflectiveQuadTo(800f, 800f)
                        close()
                        moveToRelative(0f, -80f)
                        horizontalLineToRelative(640f)
                        verticalLineToRelative(-400f)
                        horizontalLineTo(447f)
                        lineToRelative(-80f, -80f)
                        horizontalLineTo(160f)
                        close()
                        moveToRelative(0f, 0f)
                        verticalLineToRelative(-480f)
                        close()
                    }
                }.build()
                return _FOLDER!!
            }
    }
}