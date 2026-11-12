// src/main/java/com/example/metamorph/client/MorphGui.java
package com.example.metamorph.client;

import com.example.metamorph.MetamorphMod;
import com.example.metamorph.common.MorphConfig;
import com.example.metamorph.common.MorphEntry;
import com.example.metamorph.common.MorphRegistry;
import com.example.metamorph.network.MorphRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MorphGui extends GuiScreen {

    private static final int COLS = 4;
    private static final int ROWS = 5;
    private static final int PER_PAGE = COLS * ROWS;

    private int page = 0;
    private int maxPage = 0;

    private final List<MorphEntry> entries = new ArrayList<MorphEntry>();

    @Override
    public void initGui() {
        buttonList.clear();
        entries.clear();

        entries.add(new MorphEntry(0, "Unmorph", 0.6F, 1.8F));

        for (MorphEntry entry : MorphRegistry.getAll()) {
            if (MorphConfig.isAllowed(entry.id)) {
                entries.add(entry);
            }
        }

        maxPage = Math.max(0, (entries.size() - 1) / PER_PAGE);

        if (page < 0) {
            page = 0;
        }

        if (page > maxPage) {
            page = maxPage;
        }

        int startX = width / 2 - (COLS * 95) / 2;
        int startY = height / 4;

        int start = page * PER_PAGE;
        int end = Math.min(entries.size(), start + PER_PAGE);

        for (int i = start; i < end; i++) {
            MorphEntry entry = entries.get(i);
            int index = i - start;

            int col = index % COLS;
            int row = index / COLS;

            buttonList.add(new GuiButton(
                    entry.id,
                    startX + col * 95,
                    startY + row * 24,
                    90,
                    20,
                    entry.name
            ));
        }

        buttonList.add(new GuiButton(-1, width / 2 - 100, height - 32, 90, 20, "Prev"));
        buttonList.add(new GuiButton(-2, width / 2 + 10, height - 32, 90, 20, "Next"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == -1) {
            if (page > 0) {
                page--;
                initGui();
            }
            return;
        }

        if (button.id == -2) {
            if (page < maxPage) {
                page++;
                initGui();
            }
            return;
        }

        MetamorphMod.network.sendToServer(new MorphRequestMessage(button.id));
        mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(
                fontRendererObj,
                "Select Morph - Page " + (page + 1) + "/" + (maxPage + 1),
                width / 2,
                10,
                0xFFFFFF
        );

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}