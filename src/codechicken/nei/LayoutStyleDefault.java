package codechicken.nei;

import codechicken.nei.api.LayoutStyle;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.nbt.NBTTagCompound;

import static codechicken.lib.gui.GuiDraw.getStringWidth;
import static codechicken.nei.LayoutManager.*;
import static codechicken.nei.NEIClientConfig.*;

public abstract class LayoutStyleDefault extends LayoutStyle
{
    @Override
    public void layout(GuiContainer gui, VisiblityData visiblity) {
        int windowWidth = gui.width;
        int windowHeight = gui.height;
        int containerWidth = gui.xSize;
        int containerLeft = gui.guiLeft;

        reset();

        // Calculate effective item panel area (right-aligned)
        int guiRight = (containerWidth + windowWidth) / 2;
        int panelLeftEdge = guiRight + 2;
        int panelRightEdge = windowWidth - 2;
        int fullPanelWidth = panelRightEdge - panelLeftEdge;

        // Calculate effective columns based on column limit setting
        int availCols = fullPanelWidth / 18;
        int maxCols = NEIClientConfig.getItemPanelColumns();
        int effectiveCols = (maxCols > 0 && maxCols < availCols) ? maxCols : availCols;
        int effectiveWidth = effectiveCols * 18;

        // Right-align the effective panel area
        int effectiveLeft = panelRightEdge - effectiveWidth;

        // Page navigation buttons - sized to match effective item width
        int buttonW = Math.max(effectiveWidth / 3, 16);
        if (buttonW * 2 > effectiveWidth - 2)
            buttonW = Math.max((effectiveWidth - 2) / 2, 8);

        prev.y = 2;
        prev.h = 16;
        prev.w = buttonW;
        prev.x = effectiveLeft;

        next.x = panelRightEdge - buttonW;
        next.y = prev.y;
        next.w = buttonW;
        next.h = prev.h;

        // Page label centered between prev and next
        pageLabel.x = (prev.x + prev.w + next.x) / 2;
        pageLabel.y = prev.y + 5;
        pageLabel.text = "(" + itemPanel.getPage() + "/" + itemPanel.getNumPages() + ")";

        itemPanel.y = prev.h + prev.y;
        itemPanel.x = effectiveLeft;
        itemPanel.w = effectiveWidth;
        itemPanel.h = windowHeight - 15 - itemPanel.y;
        if (!canPerformAction("item"))
            itemPanel.h += 15;
        itemPanel.resize();

        more.w = more.h = less.w = less.h = 16;
        less.x = effectiveLeft;
        more.x = panelRightEdge - less.w;
        more.y = less.y = windowHeight - more.h - 2;

        quantity.x = less.x + less.w + 2;
        quantity.y = less.y;
        quantity.w = more.x - quantity.x - 2;
        quantity.h = less.h;

        options.x = isEnabled() ? 0 : 6;
        options.y = isEnabled() ? windowHeight - 22 : windowHeight - 28;
        options.w = 80;
        options.h = 22;

        delete.state = 0x4;
        if (NEIController.getDeleteMode())
            delete.state |= 1;
        else if (!visiblity.enableDeleteMode)
            delete.state |= 2;

        rain.state = 0x4;
        if (disabledActions.contains("rain"))
            rain.state |= 2;
        else if (NEIClientUtils.isRaining())
            rain.state |= 1;

        gamemode.state = 0x4;
        if (NEIClientUtils.getGamemode() != 0) {
            gamemode.state |= 0x1;
            gamemode.index = NEIClientUtils.getGamemode() - 1;
        } else {
            if (NEIClientUtils.isValidGamemode("creative"))
                gamemode.index = 0;
            else if (NEIClientUtils.isValidGamemode("creative+"))
                gamemode.index = 1;
            else if (NEIClientUtils.isValidGamemode("adventure"))
                gamemode.index = 2;
        }

        magnet.state = 0x4 | (getMagnetMode() ? 1 : 0);

        if (canPerformAction("delete"))
            layoutButton(delete);
        if (canPerformAction("rain"))
            layoutButton(rain);
        if (NEIClientUtils.isValidGamemode("creative") ||
                NEIClientUtils.isValidGamemode("creative+") ||
                NEIClientUtils.isValidGamemode("adventure"))
            layoutButton(gamemode);
        if (canPerformAction("magnet"))
            layoutButton(magnet);
        if (canPerformAction("time")) {
            for (int i = 0; i < 4; i++) {
                timeButtons[i].state = disabledActions.contains(NEIActions.timeZones[i]) ? 2 : 0;
                layoutButton(timeButtons[i]);
            }
        }
        if (canPerformAction("heal"))
            layoutButton(heal);

        searchField.y = windowHeight - searchField.h - 2;

        dropDown.h = 20;
        dropDown.w = prev.x - dropDown.x - 3;
        searchField.h = 20;
        searchField.w = 150;
        searchField.x = (windowWidth - searchField.w) / 2;

        if (!visiblity.showItemSection) {
            //TODO dropDown.setDropDown(0);
            searchField.setFocus(false);
        }

        int maxWidth = 0;
        for (int i = 0; i < 7; i++) {
            deleteButtons[i].w = 16;
            deleteButtons[i].h = 16;

            NBTTagCompound statelist = global.nbt.getCompoundTag("statename");
            global.nbt.setTag("statename", statelist);
            String name = statelist.getString("" + i);
            if (statelist.getTag("" + i) == null) {
                name = "" + (i + 1);
                statelist.setString("" + i, name);
            }
            stateButtons[i].label = name;
            stateButtons[i].saved = isStateSaved(i);

            int width = getStringWidth(stateButtons[i].getRenderLabel()) + 26;
            if (width + 22 > containerLeft)
                width = containerLeft - 22;

            if (width > maxWidth)
                maxWidth = width;
        }

        for (int i = 0; i < 7; i++) {
            stateButtons[i].x = 0;
            stateButtons[i].y = 58 + i * 22;
            stateButtons[i].h = 20;

            stateButtons[i].x = 0;
            stateButtons[i].w = maxWidth;
            deleteButtons[i].x = stateButtons[i].w + 3;
            deleteButtons[i].y = stateButtons[i].y + 2;
        }
    }

    public abstract void layoutButton(Button button);
}
