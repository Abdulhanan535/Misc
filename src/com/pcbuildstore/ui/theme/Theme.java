package com.pcbuildstore.ui.theme;

import java.awt.Color;
import java.awt.Font;

public final class Theme {

    private Theme() {}

    public static final Color BG          = new Color(0x08, 0x08, 0x0C);
    public static final Color SURFACE     = new Color(0x14, 0x14, 0x1C);
    public static final Color SURFACE_2   = new Color(0x1C, 0x1C, 0x26);
    public static final Color SURFACE_3   = new Color(0x24, 0x24, 0x30);
    public static final Color SIDEBAR     = new Color(0x0E, 0x0E, 0x14);
    public static final Color BORDER      = new Color(0x23, 0x23, 0x32);
    public static final Color BORDER_SOFT = new Color(0x1A, 0x1A, 0x24);
    public static final Color BORDER_HARD = new Color(0x2C, 0x2C, 0x3D);

    public static final Color TEXT        = new Color(0xF5, 0xF5, 0xFA);
    public static final Color TEXT_2      = new Color(0xB0, 0xB0, 0xC4);
    public static final Color TEXT_3      = new Color(0x78, 0x78, 0x91);
    public static final Color TEXT_INV    = new Color(0x0A, 0x0A, 0x12);

    public static final Color ACCENT      = new Color(0x00, 0xFF, 0xAA);
    public static final Color ACCENT_HOV  = new Color(0x00, 0xD0, 0x8A);
    public static final Color ACCENT_SOFT = new Color(0x12, 0x2A, 0x24);

    public static final Color SALE        = new Color(0xFF, 0x50, 0x28);
    public static final Color SALE_SOFT   = new Color(0x32, 0x18, 0x10);
    public static final Color EMBER       = SALE;
    public static final Color SUCCESS     = new Color(0x32, 0xC8, 0x6A);
    public static final Color SUCCESS_SOFT= new Color(0x14, 0x2A, 0x1C);
    public static final Color INFO        = new Color(0x32, 0x82, 0xFF);
    public static final Color INFO_SOFT   = new Color(0x10, 0x1E, 0x36);
    public static final Color VIOLET      = new Color(0x8C, 0x3C, 0xFF);
    public static final Color VIOLET_SOFT = new Color(0x22, 0x14, 0x38);
    public static final Color GOLD        = new Color(0xFF, 0xC8, 0x00);
    public static final Color ROSE        = new Color(0xFF, 0x32, 0x5A);
    public static final Color CYAN        = new Color(0x00, 0xC8, 0xFF);
    public static final Color MINT        = ACCENT;
    public static final Color HOT         = new Color(0xFF, 0x32, 0x5A);
    public static final Color WARN        = GOLD;
    public static final Color NEW_TAG     = new Color(0x00, 0xE6, 0x96);

    public static final Color INTEL_BLUE  = new Color(0x00, 0x71, 0xC5);
    public static final Color AMD_RED     = new Color(0xED, 0x1C, 0x24);
    public static final Color NVIDIA_GRN  = new Color(0x76, 0xB9, 0x00);
    public static final Color AMD_ORANGE  = new Color(0xF0, 0x78, 0x00);

    public static Font light(int size)   { return new Font("Segoe UI Light", Font.PLAIN, size); }
    public static Font regular(int size) { return new Font("Segoe UI", Font.PLAIN, size); }
    public static Font medium(int size)  { return new Font("Segoe UI Semibold", Font.PLAIN, size); }
    public static Font bold(int size)    { return new Font("Segoe UI", Font.BOLD, size); }
    public static Font mono(int size)    { return new Font("Consolas", Font.PLAIN, size); }

    public static final int XS  = 4;
    public static final int S   = 8;
    public static final int M   = 12;
    public static final int L   = 16;
    public static final int XL  = 24;
    public static final int XXL = 32;
    public static final int XXXL= 48;

    public static final int R_SMALL  = 6;
    public static final int R_MEDIUM = 10;
    public static final int R_LARGE  = 16;
    public static final int R_PILL   = 999;
}
