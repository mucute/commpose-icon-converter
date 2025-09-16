package com.github.mucute.commposeiconconverter.parser

/**
 * SVG文档数据类
 */
data class SvgDocument(
    val width: Float?,
    val height: Float?,
    val viewBox: ViewBox?,
    val elements: List<SvgElement>
)

/**
 * ViewBox数据类
 */
data class ViewBox(
    val minX: Float,
    val minY: Float,
    val width: Float,
    val height: Float
)

/**
 * SVG元素基类
 */
sealed class SvgElement {
    abstract val fill: String?
    abstract val stroke: String?
    abstract val strokeWidth: String?
    abstract val transform: String?
}

/**
 * SVG路径元素
 */
data class SvgPath(
    val d: String,
    override val fill: String? = null,
    override val stroke: String? = null,
    override val strokeWidth: String? = null,
    override val transform: String? = null,
    val fillRule: String? = null,
    val strokeLinecap: String? = null,
    val strokeLinejoin: String? = null
) : SvgElement()

/**
 * SVG矩形元素
 */
data class SvgRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rx: Float? = null,
    val ry: Float? = null,
    override val fill: String? = null,
    override val stroke: String? = null,
    override val strokeWidth: String? = null,
    override val transform: String? = null
) : SvgElement()

/**
 * SVG圆形元素
 */
data class SvgCircle(
    val cx: Float,
    val cy: Float,
    val r: Float,
    override val fill: String? = null,
    override val stroke: String? = null,
    override val strokeWidth: String? = null,
    override val transform: String? = null
) : SvgElement()

/**
 * SVG椭圆元素
 */
data class SvgEllipse(
    val cx: Float,
    val cy: Float,
    val rx: Float,
    val ry: Float,
    override val fill: String? = null,
    override val stroke: String? = null,
    override val strokeWidth: String? = null,
    override val transform: String? = null
) : SvgElement()

/**
 * SVG直线元素
 */
data class SvgLine(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    override val fill: String? = null,
    override val stroke: String? = null,
    override val strokeWidth: String? = null,
    override val transform: String? = null
) : SvgElement()

/**
 * SVG折线元素
 */
data class SvgPolyline(
    val points: List<Pair<Float, Float>>,
    override val fill: String? = null,
    override val stroke: String? = null,
    override val strokeWidth: String? = null,
    override val transform: String? = null
) : SvgElement()

/**
 * SVG多边形元素
 */
data class SvgPolygon(
    val points: List<Pair<Float, Float>>,
    override val fill: String? = null,
    override val stroke: String? = null,
    override val strokeWidth: String? = null,
    override val transform: String? = null
) : SvgElement()

/**
 * SVG组元素
 */
data class SvgGroup(
    val elements: List<SvgElement>,
    override val fill: String? = null,
    override val stroke: String? = null,
    override val strokeWidth: String? = null,
    override val transform: String? = null
) : SvgElement()