package my;


import my.net.neoforged.api.distmarker.Dist;
import my.net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public interface ContainerEventHandler extends GuiEventListener {
    List<? extends GuiEventListener> children();

    /**
     * Returns the first event listener that intersects with the mouse coordinates.
     */
    default Optional<GuiEventListener> getChildAt(int pMouseX, int pMouseY) {
        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.isMouseOver(pMouseX, pMouseY)) {
                return Optional.of(guieventlistener);
            }
        }

        return Optional.empty();
    }

    /**
     * Called when a mouse button is clicked within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param pMouseX the X coordinate of the mouse.
     * @param pMouseY the Y coordinate of the mouse.
     * @param pButton the button that was clicked.
     */
    @Override
    default boolean mouseClicked(int pMouseX, int pMouseY, int pButton) {
        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(pMouseX, pMouseY, pButton)) {
                this.setFocused(guieventlistener);
                if (pButton == 0) {
                    this.setDragging(true);
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Called when a mouse button is released within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param pMouseX the X coordinate of the mouse.
     * @param pMouseY the Y coordinate of the mouse.
     * @param pButton the button that was released.
     */
    @Override
    default boolean mouseReleased(int pMouseX, int pMouseY, int pButton) {
        if (pButton == 0 && this.isDragging()) {
            this.setDragging(false);
            if (this.getFocused() != null) {
                return this.getFocused().mouseReleased(pMouseX, pMouseY, pButton);
            }
        }

        return this.getChildAt(pMouseX, pMouseY).filter(p_94708_ -> p_94708_.mouseReleased(pMouseX, pMouseY, pButton)).isPresent();
    }

    /**
     * Called when the mouse is dragged within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param pMouseX the X coordinate of the mouse.
     * @param pMouseY the Y coordinate of the mouse.
     * @param pButton the button that is being dragged.
     * @param pDragX  the X distance of the drag.
     * @param pDragY  the Y distance of the drag.
     */
    @Override
    default boolean mouseDragged(int pMouseX, int pMouseY, int pButton, int pDragX, int pDragY) {
        return this.getFocused() != null && this.isDragging() && pButton == 0
                ? this.getFocused().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)
                : false;
    }

    boolean isDragging();

    /**
     * Sets if the GUI element is dragging or not.
     *
     * @param pIsDragging the dragging state of the GUI element.
     */
    void setDragging(boolean pIsDragging);

    @Override
    default boolean mouseScrolled(int pMouseX, int pMouseY, int pScrollX, int pScrollY) {
        return this.getChildAt(pMouseX, pMouseY).filter(p_293596_ -> p_293596_.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY)).isPresent();
    }

    /**
     * Called when a keyboard key is pressed within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param pKeyCode   the key code of the pressed key.
     * @param pScanCode  the scan code of the pressed key.
     * @param pModifiers the keyboard modifiers.
     */
    @Override
    default boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return this.getFocused() != null && this.getFocused().keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    /**
     * Called when a keyboard key is released within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param pKeyCode   the key code of the released key.
     * @param pScanCode  the scan code of the released key.
     * @param pModifiers the keyboard modifiers.
     */
    @Override
    default boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        return this.getFocused() != null && this.getFocused().keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    /**
     * Called when a character is typed within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param pCodePoint the code point of the typed character.
     * @param pModifiers the keyboard modifiers.
     */
    @Override
    default boolean charTyped(char pCodePoint, int pModifiers) {
        return this.getFocused() != null && this.getFocused().charTyped(pCodePoint, pModifiers);
    }

    @Nullable
    GuiEventListener getFocused();

    /**
     * Sets the focus state of the GUI element.
     *
     * @param pFocused the focused GUI element.
     */
    void setFocused(@Nullable GuiEventListener pFocused);

    /**
     * Sets the focus state of the GUI element.
     *
     * @param pFocused {@code true} to apply focus, {@code false} to remove focus
     */
    @Override
    default void setFocused(boolean pFocused) {
    }

    @Override
    default boolean isFocused() {
        return this.getFocused() != null;
    }

//    @Nullable
//    @Override
//    default ComponentPath getCurrentFocusPath() {
//        GuiEventListener guieventlistener = this.getFocused();
//        return guieventlistener != null ? ComponentPath.path(this, guieventlistener.getCurrentFocusPath()) : null;
//    }

    /**
     * Retrieves the next focus path based on the given focus navigation event.
     * <p>
     * @return The next focus path as a ComponentPath, or {@code null} if there is no next focus path.
     *
     * @param pEvent the focus navigation event.
     */
//    @Nullable
//    @Override
//    default ComponentPath nextFocusPath(FocusNavigationEvent pEvent) {
//        GuiEventListener guieventlistener = this.getFocused();
//        if (guieventlistener != null) {
//            ComponentPath componentpath = guieventlistener.nextFocusPath(pEvent);
//            if (componentpath != null) {
//                return ComponentPath.path(this, componentpath);
//            }
//        }
//
//        if (pEvent instanceof FocusNavigationEvent.TabNavigation focusnavigationevent$tabnavigation) {
//            return this.handleTabNavigation(focusnavigationevent$tabnavigation);
//        } else {
//            return pEvent instanceof FocusNavigationEvent.ArrowNavigation focusnavigationevent$arrownavigation
//                    ? this.handleArrowNavigation(focusnavigationevent$arrownavigation)
//                    : null;
//        }
//    }

    /**
     * Handles tab-based navigation events.
     * <p>
     * @return The next focus path for tab navigation, or {@code null} if no suitable path is found.
     *
     * @param pTabNavigation The tab navigation event.
     */
//    @Nullable
//    private ComponentPath handleTabNavigation(FocusNavigationEvent.TabNavigation pTabNavigation) {
//        boolean flag = pTabNavigation.forward();
//        GuiEventListener guieventlistener = this.getFocused();
//        List<? extends GuiEventListener> list = new ArrayList<>(this.children());
//        Collections.sort(list, Comparator.comparingInt(p_344153_ -> p_344153_.getTabOrderGroup()));
//        int j = list.indexOf(guieventlistener);
//        int i;
//        if (guieventlistener != null && j >= 0) {
//            i = j + (flag ? 1 : 0);
//        } else if (flag) {
//            i = 0;
//        } else {
//            i = list.size();
//        }
//
//        ListIterator<? extends GuiEventListener> listiterator = list.listIterator(i);
//        BooleanSupplier booleansupplier = flag ? listiterator::hasNext : listiterator::hasPrevious;
//        Supplier<? extends GuiEventListener> supplier = flag ? listiterator::next : listiterator::previous;
//
//        while (booleansupplier.getAsBoolean()) {
//            GuiEventListener guieventlistener1 = supplier.get();
//            ComponentPath componentpath = guieventlistener1.nextFocusPath(pTabNavigation);
//            if (componentpath != null) {
//                return ComponentPath.path(this, componentpath);
//            }
//        }
//
//        return null;
//    }

    /**
     * Handles arrow-based navigation events.
     * <p>
     * @return The next focus path for arrow navigation, or {@code null} if no suitable path is found.
     *
     * @param pArrowNavigation The arrow navigation event.
     */
//    @Nullable
//    private ComponentPath handleArrowNavigation(FocusNavigationEvent.ArrowNavigation pArrowNavigation) {
//        GuiEventListener guieventlistener = this.getFocused();
//        if (guieventlistener == null) {
//            ScreenDirection screendirection = pArrowNavigation.direction();
//            ScreenRectangle screenrectangle1 = this.getRectangle().getBorder(screendirection.getOpposite());
//            return ComponentPath.path(this, this.nextFocusPathInDirection(screenrectangle1, screendirection, null, pArrowNavigation));
//        } else {
//            ScreenRectangle screenrectangle = guieventlistener.getRectangle();
//            return ComponentPath.path(this, this.nextFocusPathInDirection(screenrectangle, pArrowNavigation.direction(), guieventlistener, pArrowNavigation));
//        }
//    }

    /**
     * Calculates the next focus path in a specific direction.
     * <p>
     * @return The next focus path in the specified direction, or {@code null} if no suitable path is found.
     *
     * @param pRectangle The screen rectangle.
     * @param pDirection The direction of navigation.
     * @param pListener  The currently focused GUI event listener.
     * @param pEvent     The focus navigation event.
     */
//    @Nullable
//    private ComponentPath nextFocusPathInDirection(
//            ScreenRectangle pRectangle, ScreenDirection pDirection, @Nullable GuiEventListener pListener, FocusNavigationEvent pEvent
//    ) {
//        ScreenAxis screenaxis = pDirection.getAxis();
//        ScreenAxis screenaxis1 = screenaxis.orthogonal();
//        ScreenDirection screendirection = screenaxis1.getPositive();
//        int i = pRectangle.getBoundInDirection(pDirection.getOpposite());
//        List<GuiEventListener> list = new ArrayList<>();
//
//        for (GuiEventListener guieventlistener : this.children()) {
//            if (guieventlistener != pListener) {
//                ScreenRectangle screenrectangle = guieventlistener.getRectangle();
//                if (screenrectangle.overlapsInAxis(pRectangle, screenaxis1)) {
//                    int j = screenrectangle.getBoundInDirection(pDirection.getOpposite());
//                    if (pDirection.isAfter(j, i)) {
//                        list.add(guieventlistener);
//                    } else if (j == i && pDirection.isAfter(screenrectangle.getBoundInDirection(pDirection), pRectangle.getBoundInDirection(pDirection))) {
//                        list.add(guieventlistener);
//                    }
//                }
//            }
//        }
//
//        Comparator<GuiEventListener> comparator = Comparator.comparing(
//                p_264674_ -> p_264674_.getRectangle().getBoundInDirection(pDirection.getOpposite()), pDirection.coordinateValueComparator()
//        );
//        Comparator<GuiEventListener> comparator1 = Comparator.comparing(
//                p_264676_ -> p_264676_.getRectangle().getBoundInDirection(screendirection.getOpposite()), screendirection.coordinateValueComparator()
//        );
//        list.sort(comparator.thenComparing(comparator1));
//
//        for (GuiEventListener guieventlistener1 : list) {
//            ComponentPath componentpath = guieventlistener1.nextFocusPath(pEvent);
//            if (componentpath != null) {
//                return componentpath;
//            }
//        }
//
//        return this.nextFocusPathVaguelyInDirection(pRectangle, pDirection, pListener, pEvent);
//    }

    /**
     * Calculates the next focus path in a vague direction.
     * <p>
     * @return The next focus path in the vague direction, or {@code null} if no suitable path is found.
     *
     * @param pRectangle The screen rectangle.
     * @param pDirection The direction of navigation.
     * @param pListener  The currently focused GUI event listener.
     * @param pEvent     The focus navigation event.
     */
//    @Nullable
//    private ComponentPath nextFocusPathVaguelyInDirection(
//            ScreenRectangle pRectangle, ScreenDirection pDirection, @Nullable GuiEventListener pListener, FocusNavigationEvent pEvent
//    ) {
//        ScreenAxis screenaxis = pDirection.getAxis();
//        ScreenAxis screenaxis1 = screenaxis.orthogonal();
//        List<Pair<GuiEventListener, Long>> list = new ArrayList<>();
//        ScreenPosition screenposition = ScreenPosition.of(screenaxis, pRectangle.getBoundInDirection(pDirection), pRectangle.getCenterInAxis(screenaxis1));
//
//        for (GuiEventListener guieventlistener : this.children()) {
//            if (guieventlistener != pListener) {
//                ScreenRectangle screenrectangle = guieventlistener.getRectangle();
//                ScreenPosition screenposition1 = ScreenPosition.of(
//                        screenaxis, screenrectangle.getBoundInDirection(pDirection.getOpposite()), screenrectangle.getCenterInAxis(screenaxis1)
//                );
//                if (pDirection.isAfter(screenposition1.getCoordinate(screenaxis), screenposition.getCoordinate(screenaxis))) {
//                    long i = Vector2i.distanceSquared(screenposition.x(), screenposition.y(), screenposition1.x(), screenposition1.y());
//                    list.add(Pair.of(guieventlistener, i));
//                }
//            }
//        }
//
//        list.sort(Comparator.comparingDouble(Pair::getSecond));
//
//        for (Pair<GuiEventListener, Long> pair : list) {
//            ComponentPath componentpath = pair.getFirst().nextFocusPath(pEvent);
//            if (componentpath != null) {
//                return componentpath;
//            }
//        }
//
//        return null;
//    }
}
