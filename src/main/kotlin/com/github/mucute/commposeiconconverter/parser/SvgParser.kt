package com.github.mucute.commposeiconconverter.parser

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * SVG解析器，用于解析SVG文件内容并提取图形元素
 */
class SvgParser {
    
    /**
     * 解析SVG字符串内容
     */
    fun parse(svgContent: String): SvgDocument {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(org.xml.sax.InputSource(StringReader(svgContent)))
        
        val svgElement = document.documentElement
        return parseSvgElement(svgElement)
    }
    
    private fun parseSvgElement(svgElement: Element): SvgDocument {
        val width = parseSize(svgElement.getAttribute("width"))
        val height = parseSize(svgElement.getAttribute("height"))
        val viewBox = parseViewBox(svgElement.getAttribute("viewBox"))
        
        val elements = mutableListOf<SvgElement>()
        parseChildElements(svgElement, elements)
        
        return SvgDocument(
            width = width,
            height = height,
            viewBox = viewBox,
            elements = elements
        )
    }
    
    private fun parseChildElements(parent: Element, elements: MutableList<SvgElement>) {
        val children = parent.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child.nodeType == Node.ELEMENT_NODE) {
                val element = child as Element
                when (element.tagName.lowercase()) {
                    "path" -> elements.add(parsePath(element))
                    "rect" -> elements.add(parseRect(element))
                    "circle" -> elements.add(parseCircle(element))
                    "ellipse" -> elements.add(parseEllipse(element))
                    "line" -> elements.add(parseLine(element))
                    "polyline" -> elements.add(parsePolyline(element))
                    "polygon" -> elements.add(parsePolygon(element))
                    "g" -> {
                        val group = parseGroup(element)
                        elements.add(group)
                    }
                }
            }
        }
    }
    
    private fun parsePath(element: Element): SvgPath {
        return SvgPath(
            d = element.getAttribute("d"),
            fill = element.getAttribute("fill"),
            stroke = element.getAttribute("stroke"),
            strokeWidth = element.getAttribute("stroke-width"),
            transform = element.getAttribute("transform"),
            fillRule = element.getAttribute("fill-rule"),
            strokeLinecap = element.getAttribute("stroke-linecap"),
            strokeLinejoin = element.getAttribute("stroke-linejoin")
        )
    }
    
    private fun parseRect(element: Element): SvgRect {
        return SvgRect(
            x = element.getAttribute("x").toFloatOrNull() ?: 0f,
            y = element.getAttribute("y").toFloatOrNull() ?: 0f,
            width = element.getAttribute("width").toFloatOrNull() ?: 0f,
            height = element.getAttribute("height").toFloatOrNull() ?: 0f,
            rx = element.getAttribute("rx").toFloatOrNull(),
            ry = element.getAttribute("ry").toFloatOrNull(),
            fill = element.getAttribute("fill"),
            stroke = element.getAttribute("stroke"),
            strokeWidth = element.getAttribute("stroke-width"),
            transform = element.getAttribute("transform")
        )
    }
    
    private fun parseCircle(element: Element): SvgCircle {
        return SvgCircle(
            cx = element.getAttribute("cx").toFloatOrNull() ?: 0f,
            cy = element.getAttribute("cy").toFloatOrNull() ?: 0f,
            r = element.getAttribute("r").toFloatOrNull() ?: 0f,
            fill = element.getAttribute("fill"),
            stroke = element.getAttribute("stroke"),
            strokeWidth = element.getAttribute("stroke-width"),
            transform = element.getAttribute("transform")
        )
    }
    
    private fun parseEllipse(element: Element): SvgEllipse {
        return SvgEllipse(
            cx = element.getAttribute("cx").toFloatOrNull() ?: 0f,
            cy = element.getAttribute("cy").toFloatOrNull() ?: 0f,
            rx = element.getAttribute("rx").toFloatOrNull() ?: 0f,
            ry = element.getAttribute("ry").toFloatOrNull() ?: 0f,
            fill = element.getAttribute("fill"),
            stroke = element.getAttribute("stroke"),
            strokeWidth = element.getAttribute("stroke-width"),
            transform = element.getAttribute("transform")
        )
    }
    
    private fun parseLine(element: Element): SvgLine {
        return SvgLine(
            x1 = element.getAttribute("x1").toFloatOrNull() ?: 0f,
            y1 = element.getAttribute("y1").toFloatOrNull() ?: 0f,
            x2 = element.getAttribute("x2").toFloatOrNull() ?: 0f,
            y2 = element.getAttribute("y2").toFloatOrNull() ?: 0f,
            stroke = element.getAttribute("stroke"),
            strokeWidth = element.getAttribute("stroke-width"),
            transform = element.getAttribute("transform")
        )
    }
    
    private fun parsePolyline(element: Element): SvgPolyline {
        return SvgPolyline(
            points = parsePoints(element.getAttribute("points")),
            fill = element.getAttribute("fill"),
            stroke = element.getAttribute("stroke"),
            strokeWidth = element.getAttribute("stroke-width"),
            transform = element.getAttribute("transform")
        )
    }
    
    private fun parsePolygon(element: Element): SvgPolygon {
        return SvgPolygon(
            points = parsePoints(element.getAttribute("points")),
            fill = element.getAttribute("fill"),
            stroke = element.getAttribute("stroke"),
            strokeWidth = element.getAttribute("stroke-width"),
            transform = element.getAttribute("transform")
        )
    }
    
    private fun parseGroup(element: Element): SvgGroup {
        val elements = mutableListOf<SvgElement>()
        parseChildElements(element, elements)
        
        return SvgGroup(
            elements = elements,
            transform = element.getAttribute("transform")
        )
    }
    
    private fun parseSize(sizeStr: String): Float? {
        if (sizeStr.isBlank()) return null
        
        // 移除单位后缀 (px, pt, em, etc.)
        val cleanStr = sizeStr.replace(Regex("[a-zA-Z%]+$"), "")
        return cleanStr.toFloatOrNull()
    }
    
    private fun parseViewBox(viewBoxStr: String): ViewBox? {
        if (viewBoxStr.isBlank()) return null
        
        val values = viewBoxStr.trim().split(Regex("\\s+|,")).mapNotNull { it.toFloatOrNull() }
        return if (values.size == 4) {
            ViewBox(values[0], values[1], values[2], values[3])
        } else null
    }
    
    private fun parsePoints(pointsStr: String): List<Pair<Float, Float>> {
        if (pointsStr.isBlank()) return emptyList()
        
        val points = mutableListOf<Pair<Float, Float>>()
        val values = pointsStr.trim().split(Regex("\\s+|,")).mapNotNull { it.toFloatOrNull() }
        
        for (i in 0 until values.size step 2) {
            if (i + 1 < values.size) {
                points.add(Pair(values[i], values[i + 1]))
            }
        }
        
        return points
    }
}