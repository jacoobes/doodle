package com.nectar.doodle.system.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.translate
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.system.Cursor
import com.nectar.doodle.system.SystemInputEvent.Modifier
import com.nectar.doodle.system.SystemMouseEvent
import com.nectar.doodle.system.SystemMouseEvent.Button.Button1
import com.nectar.doodle.system.SystemMouseEvent.Button.Button2
import com.nectar.doodle.system.SystemMouseEvent.Button.Button3
import com.nectar.doodle.system.SystemMouseEvent.Type
import com.nectar.doodle.system.SystemMouseEvent.Type.Down
import com.nectar.doodle.system.SystemMouseEvent.Type.Enter
import com.nectar.doodle.system.SystemMouseEvent.Type.Exit
import com.nectar.doodle.system.SystemMouseEvent.Type.Move
import com.nectar.doodle.system.SystemMouseEvent.Type.Up
import com.nectar.doodle.system.SystemMouseWheelEvent
import com.nectar.doodle.system.impl.MouseInputServiceStrategy.EventHandler
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent


internal class MouseInputServiceStrategyWebkit(private val htmlFactory: HtmlFactory): MouseInputServiceStrategy {

    override var toolTipText: String = ""
        set(new) {
            inputDevice?.title = new
        }

    override var cursor: Cursor = Cursor.Default
        set(new) {
            if (new != field) {
                inputDevice!!.style.cursor = new.toString()

                field = new
            }
    }

    override val mouseLocation get() = Point(mouseX, mouseY)

    private var overlay      : HTMLElement? = null
    private var inputDevice  : HTMLElement? = null
    private var eventHandler : EventHandler? = null
    private var mouseX       : Double = 0.0
    private var mouseY       : Double = 0.0

    override fun startUp(handler: EventHandler) {
        eventHandler = handler

        if (inputDevice == null) {
            inputDevice  = htmlFactory.body
            eventHandler = handler

            registerCallbacks(this, inputDevice!!)
        }

        if (overlay == null) {
            overlay = htmlFactory.create()

            overlay?.style?.opacity = "0"
            overlay?.style?.width   = "2px"
            overlay?.style?.height  = "2px"
            overlay?.style?.zIndex  = "3"

            htmlFactory.body.add(overlay!!)
        }
    }

    override fun shutdown() {
        inputDevice?.let {
            unregisterCallbacks(it)

            inputDevice = null
        }
    }

    private fun mouseEnter(event: MouseEvent) {
        eventHandler!!.handle(createMouseEvent(event, Enter, 0))
    }

    private fun mouseExit(event: MouseEvent) {
        eventHandler!!.handle(createMouseEvent(event, Exit, 0))
    }

    private fun mouseUp(event: MouseEvent): Boolean {
        eventHandler!!.handle(createMouseEvent(event, Up, 1))

        if (isNativeElement(event.target as Node)) {
            return true
        }

        event.preventDefault ()
        event.stopPropagation()

        return false
    }

    private fun mouseDown(event: MouseEvent): Boolean {
        eventHandler!!.handle(createMouseEvent(event, Down, 1))

        if (isNativeElement(event.target as Node)) {
            return true
        }

        event.preventDefault ()
        event.stopPropagation()

        return false
    }

    private fun doubleClick(event: MouseEvent): Boolean {
        eventHandler!!.handle(createMouseEvent(event, Up, 2))

        if (isNativeElement(event.target as Node)) {
            return true
        }

        event.preventDefault ()
        event.stopPropagation()

        return false
    }

    private fun mouseMove(event: MouseEvent): Boolean {
        mouseX = event.clientX + htmlFactory.body.scrollLeft
        mouseY = event.clientY + htmlFactory.body.scrollLeft

        val isNativeElement = isNativeElement(event.target as Node)

        if (!isNativeElement) {
            overlay!!.style.translate(mouseX - 1, mouseY - 1)
        }

        eventHandler!!.handle(createMouseEvent(event, Move, 0))

        if (isNativeElement) {
            return true
        }

        event.preventDefault ()
        event.stopPropagation()

        return false
    }

    private fun mouseScroll(event: WheelEvent): Boolean {
        val deltaX = 0 - event.deltaX / 28
        val deltaY = 0 - event.deltaY / 28

        val wheelEvent = SystemMouseWheelEvent(
                Point(mouseX, mouseY),
                deltaX.toInt(),
                deltaY.toInt(),
                createModifiers(event))

        eventHandler!!.handle(wheelEvent)

        return !wheelEvent.consumed.also {
            event.preventDefault ()
            event.stopPropagation()
        }
    }

    private fun createMouseEvent(event: MouseEvent, aType: Type, clickCount: Int): SystemMouseEvent {
        val buttons    = mutableSetOf<SystemMouseEvent.Button>()
        val buttonsInt = event.buttons.toInt()

        if (buttonsInt and 1 == 1) buttons.add(Button1)
        if (buttonsInt and 2 == 2) buttons.add(Button2)
        if (buttonsInt and 4 == 4) buttons.add(Button3)

        return SystemMouseEvent(
                aType,
                Point(mouseLocation.x, mouseLocation.y),
                buttons,
                clickCount,
                createModifiers(event))
    }

    private fun createModifiers(event: MouseEvent): Set<Modifier> {
        val modifiers = mutableSetOf<Modifier>()

        if (event.altKey) {
            modifiers.add(Modifier.Alt)
        }
        if (event.ctrlKey) {
            modifiers.add(Modifier.Ctrl)
        }
        if (event.shiftKey) {
            modifiers.add(Modifier.Shift)
        }

        return modifiers
    }


    private fun registerCallbacks(handler: MouseInputServiceStrategyWebkit, element: HTMLElement) {
        element.onmouseup   = { handler.mouseUp    (it as MouseEvent) }
        element.onmousedown = { handler.mouseDown  (it as MouseEvent) }
        element.onmousemove = { handler.mouseMove  (it as MouseEvent) }
        element.ondblclick  = { handler.doubleClick(it as MouseEvent) }
        element.onwheel     = { handler.mouseScroll(it as WheelEvent) }
        element.onmouseout  = { handler.mouseExit  (it as MouseEvent) }
        element.onmouseover = { handler.mouseEnter (it as MouseEvent) }
    }

    private fun unregisterCallbacks(element: HTMLElement) {
        element.onmouseup   = null
        element.onmousedown = null
        element.onmousemove = null
        element.ondblclick  = null
        element.onwheel     = null
        element.onmouseout  = null
        element.onmouseover = null
    }
}
