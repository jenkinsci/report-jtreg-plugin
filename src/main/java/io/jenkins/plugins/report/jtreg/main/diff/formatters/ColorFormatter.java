/*
 * The MIT License
 *
 * Copyright 2016 jvanek.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.report.jtreg.main.diff.formatters;

import java.io.PrintStream;

public class ColorFormatter extends StringMappedFormatter {
//those are NOT spaces but bash \e

    private static final String Default = "[39m";

    private static final String ResetAll = "[0m";

    private static final String Bold = "[1m";

    private static final String Black = "[30m";
    private static final String Red = "[31m";
    private static final String Green = "[32m";
    private static final String Yellow = "[33m";
    private static final String Blue = "[34m";
    private static final String Magenta = "[35m";
    private static final String Cyan = "[36m";
    private static final String LightRed = "[91m";
    private static final String LightGreen = "[92m";
    private static final String LightYellow = "[93m";
    private static final String LightBlue = "[94m";
    private static final String LightMagenta = "[95m";
    private static final String LightCyan = "[96m";

    private void fillColors() {
        colors.put(SupportedColors.Black, Black);
        colors.put(SupportedColors.Red, Red);
        colors.put(SupportedColors.Green, Green);
        colors.put(SupportedColors.Yellow, Yellow);
        colors.put(SupportedColors.Blue, Blue);
        colors.put(SupportedColors.Magenta, Magenta);
        colors.put(SupportedColors.Cyan, Cyan);
        colors.put(SupportedColors.LightRed, LightRed);
        colors.put(SupportedColors.LightGreen, LightGreen);
        colors.put(SupportedColors.LightYellow, LightYellow);
        colors.put(SupportedColors.LightBlue, LightBlue);
        colors.put(SupportedColors.LightMagenta, LightMagenta);
        colors.put(SupportedColors.LightCyan, LightCyan);
    }

    public ColorFormatter(PrintStream stream) {
        super(stream);
        fillColors();
    }

    @Override
    public void startBold() {
        print(Bold);
    }

    @Override
    public void startColor(SupportedColors color) {
        print(getColor(color));
    }

    @Override
    public void reset() {
        print(ResetAll);
        preset();
    }

    @Override
    public void initDoc() {
        reset();
    }

    @Override
    public void closeDoc() {
        print(ResetAll);
    }

    private void preset() {
        //sets black background and white font
        print("[49m");
        print("[97m");
    }

    @Override
    public void startTitle2() {
        startColor(SupportedColors.Cyan);
    }

    @Override
    public void startTitle1() {
        startColor(SupportedColors.Cyan);
        startBold();
    }

    @Override
    public void startTitle3() {
        startColor(SupportedColors.LightBlue);
    }

    @Override
    public void startTitle4() {
        startColor(SupportedColors.Blue);
    }

}
