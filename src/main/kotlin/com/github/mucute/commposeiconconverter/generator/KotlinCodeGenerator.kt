package com.github.mucute.commposeiconconverter.generator

import com.github.mucute.commposeiconconverter.parser.SvgDocument
import com.github.mucute.commposeiconconverter.parser.SvgElement
import com.github.mucute.commposeiconconverter.parser.SvgPath
import com.github.mucute.commposeiconconverter.parser.SvgRect
import com.github.mucute.commposeiconconverter.parser.SvgCircle
import com.github.mucute.commposeiconconverter.parser.SvgEllipse
import com.github.mucute.commposeiconconverter.parser.SvgLine
import com.github.mucute.commposeiconconverter.parser.SvgPolyline
import com.github.mucute.commposeiconconverter.parser.SvgPolygon
import com.github.mucute.commposeiconconverter.parser.SvgGroup
import com.github.mucute.commposeiconconverter.utils.ColorUtils
import com.github.mucute.commposeiconconverter.utils.PathDataParser

/**
 * SVG路径数据类
 */
data class SvgPathData(
    val pathCommands: List<String>,
    val fill: String? = null,
    val stroke: String? = null,
    val strokeWidth: Double? = null,
    val fillRule: String? = null,
    val strokeLinecap: String? = null,
    val strokeLinejoin: String? = null
)

/**
 * SVG图标信息类
 */
data class SvgIconInfo(
    val name: String,
    val width: Double,
    val height: Double,
    val viewportWidth: Double,
    val viewportHeight: Double,
    val paths: List<SvgPathData>
)

class KotlinCodeGenerator {

    fun generateKotlinFile(
        svgDocument: SvgDocument,
        iconName: String,
        packageName: String = "com.example.icons",
        iconParentClass: String? = null,
        iconStyle: String? = null
    ): String {
        val className = iconName.toCamelCase()
        val iconInfo = extractIconInfo(svgDocument, iconName)
        val propertyName = "_${iconName.toCamelCase(firstLowerCase = true)}"
        
        // 构建完整的属性名：图标类名.图标样式.图标名
        val fullPropertyName = buildString {
            iconParentClass?.let { 
                append(it.substringAfterLast('.'))  // 只取类名，不包含包名
                append(".")
            }
            iconStyle?.let {
                append(it)
                append(".")
            }
            append(className)
        }

        return buildString {
            // 包声明（如果包名不为空）
            if (packageName.isNotEmpty()) {
                appendLine("package $packageName")
                appendLine()
            }

            // 导入语句
            appendLine("import androidx.compose.ui.graphics.Color")
            appendLine("import androidx.compose.ui.graphics.PathFillType.Companion.NonZero")
            appendLine("import androidx.compose.ui.graphics.SolidColor")
            appendLine("import androidx.compose.ui.graphics.StrokeCap.Companion.Butt")
            appendLine("import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter")
            appendLine("import androidx.compose.ui.graphics.vector.ImageVector")
            appendLine("import androidx.compose.ui.graphics.vector.ImageVector.Builder")
            appendLine("import androidx.compose.ui.graphics.vector.path")
            appendLine("import androidx.compose.ui.unit.dp")
            appendLine()

            // 生成ImageVector属性
            appendLine("public val $fullPropertyName: ImageVector")
            appendLine("    get() {")
            appendLine("        if ($propertyName != null) {")
            appendLine("            return $propertyName!!")
            appendLine("        }")
            appendLine("        $propertyName = Builder(name = \"$fullPropertyName\", defaultWidth = ")
            appendLine("                ${formatFloat(iconInfo.width)}.dp, defaultHeight = ${formatFloat(iconInfo.height)}.dp, viewportWidth = ")
            appendLine("                ${formatFloat(iconInfo.viewportWidth)}f, viewportHeight = ")
            appendLine("                ${formatFloat(iconInfo.viewportHeight)}f).apply {")

            // 生成路径
            iconInfo.paths.forEach { pathData ->
                appendLine(
                    "            path(fill = ${generateFillColor(pathData.fill)}, stroke = ${
                        generateStrokeColor(
                            pathData.stroke
                        )
                    }, strokeLineWidth = ${pathData.strokeWidth ?: 0.0}f,"
                )
                appendLine(
                    "                    strokeLineCap = ${generateStrokeCap(pathData.strokeLinecap)}, strokeLineJoin = ${
                        generateStrokeJoin(
                            pathData.strokeLinejoin
                        )
                    }, strokeLineMiter = 4.0f,"
                )
                appendLine("                    pathFillType = ${generatePathFillType(pathData.fillRule)}) {")

                // 生成路径命令
                pathData.pathCommands.forEach { command ->
                    appendLine("                $command")
                }

                appendLine("            }")
            }

            appendLine("        }")
            appendLine("        .build()")
            appendLine("        return $propertyName!!")
            appendLine("    }")
            appendLine()

            // 生成私有变量
            appendLine("private var $propertyName: ImageVector? = null")
        }
    }

    /**
     * 生成填充颜色
     */
    private fun generateFillColor(fill: String?): String {
        return when (fill) {
            null, "none" -> "null"
            else -> {
                val color = ColorUtils.parseColor(fill)
                "SolidColor(Color(${color}))"
            }
        }
    }

    /**
     * 生成描边颜色
     */
    private fun generateStrokeColor(stroke: String?): String {
        return when (stroke) {
            null, "none", "" -> "null"
            else -> {
                val color = ColorUtils.parseColor(stroke)
                "SolidColor(Color(${color}))"
            }
        }
    }

    /**
     * 生成描边端点样式
     */
    private fun generateStrokeCap(strokeLinecap: String?): String {
        return when (strokeLinecap?.lowercase()) {
            "round" -> "StrokeCap.Round"
            "square" -> "StrokeCap.Square"
            else -> "Butt"
        }
    }

    /**
     * 生成描边连接样式
     */
    private fun generateStrokeJoin(strokeLinejoin: String?): String {
        return when (strokeLinejoin?.lowercase()) {
            "round" -> "StrokeJoin.Round"
            "bevel" -> "StrokeJoin.Bevel"
            else -> "Miter"
        }
    }

    /**
     * 生成路径填充类型
     */
    private fun generatePathFillType(fillRule: String?): String {
        return when (fillRule?.lowercase()) {
            "evenodd" -> "PathFillType.EvenOdd"
            else -> "NonZero"
        }
    }

    private fun extractIconInfo(svgDocument: SvgDocument, iconName: String): SvgIconInfo {
        // 优先使用SVG的width和height属性，如果没有则使用viewBox
        val width = svgDocument.width?.toDouble() ?: svgDocument.viewBox?.width?.toDouble() ?: 24.0
        val height = svgDocument.height?.toDouble() ?: svgDocument.viewBox?.height?.toDouble() ?: 24.0

        // viewport应该使用viewBox的尺寸，如果没有viewBox则使用width/height
        val viewportWidth = svgDocument.viewBox?.width?.toDouble() ?: width
        val viewportHeight = svgDocument.viewBox?.height?.toDouble() ?: height

        val paths = mutableListOf<SvgPathData>()

        extractPathsFromElements(svgDocument.elements, paths)

        return SvgIconInfo(
            name = iconName,
            width = width,
            height = height,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            paths = paths
        )
    }

    private fun extractPathsFromElements(elements: List<SvgElement>, paths: MutableList<SvgPathData>) {
        elements.forEach { element ->
            when (element) {
                is SvgPath -> {
                    element.d?.let { pathData ->
                        val pathCommands = PathDataParser.parsePathData(pathData)
                        paths.add(
                            SvgPathData(
                                pathCommands = pathCommands,
                                fill = element.fill,
                                stroke = element.stroke,
                                strokeWidth = element.strokeWidth?.toDoubleOrNull(),
                                fillRule = element.fillRule,
                                strokeLinecap = element.strokeLinecap,
                                strokeLinejoin = element.strokeLinejoin
                            )
                        )
                    }
                }

                is SvgRect -> {
                    val pathCommands = generateRectCommands(element)
                    paths.add(
                        SvgPathData(
                            pathCommands = pathCommands,
                            fill = element.fill,
                            stroke = element.stroke,
                            strokeWidth = element.strokeWidth?.toDoubleOrNull()
                        )
                    )
                }

                is SvgCircle -> {
                    val pathCommands = generateCircleCommands(element)
                    paths.add(
                        SvgPathData(
                            pathCommands = pathCommands,
                            fill = element.fill,
                            stroke = element.stroke,
                            strokeWidth = element.strokeWidth?.toDoubleOrNull()
                        )
                    )
                }

                is SvgEllipse -> {
                    val pathCommands = generateEllipseCommands(element)
                    paths.add(
                        SvgPathData(
                            pathCommands = pathCommands,
                            fill = element.fill,
                            stroke = element.stroke,
                            strokeWidth = element.strokeWidth?.toDoubleOrNull()
                        )
                    )
                }

                is SvgLine -> {
                    val pathCommands = generateLineCommands(element)
                    paths.add(
                        SvgPathData(
                            pathCommands = pathCommands,
                            stroke = element.stroke,
                            strokeWidth = element.strokeWidth?.toDoubleOrNull()
                        )
                    )
                }

                is SvgPolyline -> {
                    val pathCommands = generatePolylineCommands(element)
                    paths.add(
                        SvgPathData(
                            pathCommands = pathCommands,
                            fill = element.fill,
                            stroke = element.stroke,
                            strokeWidth = element.strokeWidth?.toDoubleOrNull()
                        )
                    )
                }

                is SvgPolygon -> {
                    val pathCommands = generatePolygonCommands(element)
                    paths.add(
                        SvgPathData(
                            pathCommands = pathCommands,
                            fill = element.fill,
                            stroke = element.stroke,
                            strokeWidth = element.strokeWidth?.toDoubleOrNull()
                        )
                    )
                }

                is SvgGroup -> {
                    // 递归处理组内元素
                    extractPathsFromElements(element.elements, paths)
                }
            }
        }
    }

    private fun generateRectCommands(rect: SvgRect): List<String> {
        val x = rect.x.toDouble()
        val y = rect.y.toDouble()
        val width = rect.width.toDouble()
        val height = rect.height.toDouble()
        val rx = rect.rx?.toDouble() ?: 0.0
        val ry = rect.ry?.toDouble() ?: 0.0

        return if (rx > 0 || ry > 0) {
            generateRoundedRectCommands(x, y, width, height, rx, ry)
        } else {
            listOf(
                "moveTo(${formatFloat(x)}f, ${formatFloat(y)}f)",
                "lineTo(${formatFloat(x + width)}f, ${formatFloat(y)}f)",
                "lineTo(${formatFloat(x + width)}f, ${formatFloat(y + height)}f)",
                "lineTo(${formatFloat(x)}f, ${formatFloat(y + height)}f)",
                "close()"
            )
        }
    }

    private fun generateCircleCommands(circle: SvgCircle): List<String> {
        val cx = circle.cx.toDouble()
        val cy = circle.cy.toDouble()
        val r = circle.r.toDouble()

        val c = 0.552284749831 * r

        return listOf(
            "moveTo(${formatFloat(cx)}f, ${formatFloat(cy - r)}f)",
            "curveTo(${formatFloat(cx + c)}f, ${formatFloat(cy - r)}f, ${formatFloat(cx + r)}f, ${formatFloat(cy - c)}f, ${
                formatFloat(
                    cx + r
                )
            }f, ${formatFloat(cy)}f)",
            "curveTo(${formatFloat(cx + r)}f, ${formatFloat(cy + c)}f, ${formatFloat(cx + c)}f, ${formatFloat(cy + r)}f, ${
                formatFloat(
                    cx
                )
            }f, ${formatFloat(cy + r)}f)",
            "curveTo(${formatFloat(cx - c)}f, ${formatFloat(cy + r)}f, ${formatFloat(cx - r)}f, ${formatFloat(cy + c)}f, ${
                formatFloat(
                    cx - r
                )
            }f, ${formatFloat(cy)}f)",
            "curveTo(${formatFloat(cx - r)}f, ${formatFloat(cy - c)}f, ${formatFloat(cx - c)}f, ${formatFloat(cy - r)}f, ${
                formatFloat(
                    cx
                )
            }f, ${formatFloat(cy - r)}f)",
            "close()"
        )
    }

    private fun generateEllipseCommands(ellipse: SvgEllipse): List<String> {
        val cx = ellipse.cx.toDouble()
        val cy = ellipse.cy.toDouble()
        val rx = ellipse.rx.toDouble()
        val ry = ellipse.ry.toDouble()

        val cx_offset = 0.552284749831 * rx
        val cy_offset = 0.552284749831 * ry

        return listOf(
            "moveTo(${formatFloat(cx)}f, ${formatFloat(cy - ry)}f)",
            "curveTo(${formatFloat(cx + cx_offset)}f, ${formatFloat(cy - ry)}f, ${formatFloat(cx + rx)}f, ${
                formatFloat(
                    cy - cy_offset
                )
            }f, ${formatFloat(cx + rx)}f, ${formatFloat(cy)}f)",
            "curveTo(${formatFloat(cx + rx)}f, ${formatFloat(cy + cy_offset)}f, ${formatFloat(cx + cx_offset)}f, ${
                formatFloat(
                    cy + ry
                )
            }f, ${formatFloat(cx)}f, ${formatFloat(cy + ry)}f)",
            "curveTo(${formatFloat(cx - cx_offset)}f, ${formatFloat(cy + ry)}f, ${formatFloat(cx - rx)}f, ${
                formatFloat(
                    cy + cy_offset
                )
            }f, ${formatFloat(cx - rx)}f, ${formatFloat(cy)}f)",
            "curveTo(${formatFloat(cx - rx)}f, ${formatFloat(cy - cy_offset)}f, ${formatFloat(cx - cx_offset)}f, ${
                formatFloat(
                    cy - ry
                )
            }f, ${formatFloat(cx)}f, ${formatFloat(cy - ry)}f)",
            "close()"
        )
    }

    private fun generateLineCommands(line: SvgLine): List<String> {
        val x1 = line.x1.toDouble()
        val y1 = line.y1.toDouble()
        val x2 = line.x2.toDouble()
        val y2 = line.y2.toDouble()

        return listOf(
            "moveTo(${formatFloat(x1)}f, ${formatFloat(y1)}f)",
            "lineTo(${formatFloat(x2)}f, ${formatFloat(y2)}f)"
        )
    }

    private fun generatePolylineCommands(polyline: SvgPolyline): List<String> {
        val commands = mutableListOf<String>()

        if (polyline.points.isNotEmpty()) {
            val firstPoint = polyline.points.first()
            commands.add("moveTo(${formatFloat(firstPoint.first.toDouble())}f, ${formatFloat(firstPoint.second.toDouble())}f)")

            for (i in 1 until polyline.points.size) {
                val point = polyline.points[i]
                commands.add("lineTo(${formatFloat(point.first.toDouble())}f, ${formatFloat(point.second.toDouble())}f)")
            }
        }

        return commands
    }

    private fun generatePolygonCommands(polygon: SvgPolygon): List<String> {
        val commands = mutableListOf<String>()

        if (polygon.points.isNotEmpty()) {
            val firstPoint = polygon.points.first()
            commands.add("moveTo(${formatFloat(firstPoint.first.toDouble())}f, ${formatFloat(firstPoint.second.toDouble())}f)")

            for (i in 1 until polygon.points.size) {
                val point = polygon.points[i]
                commands.add("lineTo(${formatFloat(point.first.toDouble())}f, ${formatFloat(point.second.toDouble())}f)")
            }

            commands.add("close()")
        }

        return commands
    }

    private fun generateRoundedRectCommands(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        rx: Double,
        ry: Double
    ): List<String> {
        val actualRx = minOf(rx, width / 2)
        val actualRy = minOf(ry, height / 2)
        val commands = mutableListOf<String>()

        commands.add("moveTo(${formatFloat(x + actualRx)}f, ${formatFloat(y)}f)")
        commands.add("lineTo(${formatFloat(x + width - actualRx)}f, ${formatFloat(y)}f)")

        if (actualRx > 0 && actualRy > 0) {
            val c1 = 0.552284749831 * actualRx
            val c2 = 0.552284749831 * actualRy
            commands.add(
                "curveTo(${formatFloat(x + width - actualRx + c1)}f, ${formatFloat(y)}f, ${formatFloat(x + width)}f, ${
                    formatFloat(
                        y + actualRy - c2
                    )
                }f, ${formatFloat(x + width)}f, ${formatFloat(y + actualRy)}f)"
            )
        }

        commands.add("lineTo(${formatFloat(x + width)}f, ${formatFloat(y + height - actualRy)}f)")

        if (actualRx > 0 && actualRy > 0) {
            val c1 = 0.552284749831 * actualRx
            val c2 = 0.552284749831 * actualRy
            commands.add(
                "curveTo(${formatFloat(x + width)}f, ${formatFloat(y + height - actualRy + c2)}f, ${
                    formatFloat(x + width - actualRx + c1)
                }f, ${formatFloat(y + height)}f, ${formatFloat(x + width - actualRx)}f, ${formatFloat(y + height)}f)"
            )
        }

        commands.add("lineTo(${formatFloat(x + actualRx)}f, ${formatFloat(y + height)}f)")

        if (actualRx > 0 && actualRy > 0) {
            val c1 = 0.552284749831 * actualRx
            val c2 = 0.552284749831 * actualRy
            commands.add(
                "curveTo(${formatFloat(x + actualRx - c1)}f, ${formatFloat(y + height)}f, ${formatFloat(x)}f, ${
                    formatFloat(y + height - actualRy + c2)
                }f, ${formatFloat(x)}f, ${formatFloat(y + height - actualRy)}f)"
            )
        }

        commands.add("lineTo(${formatFloat(x)}f, ${formatFloat(y + actualRy)}f)")

        if (actualRx > 0 && actualRy > 0) {
            val c1 = 0.552284749831 * actualRx
            val c2 = 0.552284749831 * actualRy
            commands.add(
                "curveTo(${formatFloat(x)}f, ${formatFloat(y + actualRy - c2)}f, ${formatFloat(x + actualRx - c1)}f, ${
                    formatFloat(y)
                }f, ${formatFloat(x + actualRx)}f, ${formatFloat(y)}f)"
            )
        }

        commands.add("close()")
        return commands
    }

    private fun formatFloat(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            String.format("%.2f", value).trimEnd('0').trimEnd('.')
        }
    }

    private fun String.toCamelCase(firstLowerCase: Boolean = false): String {
        val words = this.split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.isNotEmpty() }
            .map { it.lowercase().replaceFirstChar { char -> char.uppercase() } }

        return if (words.isEmpty()) {
            ""
        } else {
            val first = if (firstLowerCase) words.first().replaceFirstChar { it.lowercase() } else words.first()
            first + words.drop(1).joinToString("")
        }
    }
}