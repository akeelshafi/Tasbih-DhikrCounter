"""
Generates the Tasbih app icon from one set of geometry:
  - Android adaptive icon (background / foreground / monochrome vector drawables)
  - play_store/icon_512.png  (Play Console app icon)
  - play_store/feature_graphic_1024x500.png

Run:  python3 tools/make_icon.py        (needs Pillow + numpy)
"""
import math, os
import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app/src/main/res")
OUT = os.path.join(ROOT, "play_store")

# ---------------------------------------------------------------- geometry (108 x 108 viewport)
CX, CY = 54.0, 44.5      # centre of the bead ring
R = 19.0                 # ring radius
N = 14                   # beads in the ring
BEAD_R = 4.3
MARK_R = 5.6             # the larger marker bead at the bottom, where the tassel hangs

PEARL = ((255, 255, 255), (244, 236, 216), (184, 169, 138))   # light, base, dark
JADE = ((166, 240, 212), (47, 165, 132), (11, 74, 58))
GOLD = ((255, 236, 176), (224, 184, 90), (138, 101, 28))

def hexs(c): return "#%02X%02X%02X" % c

beads = []
for k in range(N):
    th = math.radians(90 + k * 360 / N)
    x, y = CX + R * math.cos(th), CY + R * math.sin(th)
    if k == 0:
        beads.append((x, y, MARK_R, GOLD, True))
    else:
        beads.append((x, y, BEAD_R, PEARL if k % 2 else JADE, False))

# crescent (outer circle minus inner circle), sampled as a polygon so PNG and vector match exactly
def crescent():
    c1, r1 = (CX - 1.5, CY), 9.6
    c2, r2 = (CX + 2.2, CY - 1.6), 8.0
    d = math.dist(c1, c2)
    a = (r1 * r1 - r2 * r2 + d * d) / (2 * d)
    beta = math.acos(a / r1)
    gamma = math.acos((d - a) / r2)
    phi = math.atan2(c2[1] - c1[1], c2[0] - c1[0])
    psi = math.atan2(c1[1] - c2[1], c1[0] - c2[0])
    outer = [(c1[0] + r1 * math.cos(t), c1[1] + r1 * math.sin(t))
             for t in np.linspace(phi + beta, phi + 2 * math.pi - beta, 60)]
    inner = [(c2[0] + r2 * math.cos(t), c2[1] + r2 * math.sin(t))
             for t in np.linspace(psi - gamma, psi + gamma, 40)]
    if math.dist(inner[0], outer[-1]) > math.dist(inner[-1], outer[-1]):
        inner.reverse()
    return outer + inner

def star(cx, cy, ro, ri, rot=-18):
    pts = []
    for i in range(10):
        r = ro if i % 2 == 0 else ri
        a = math.radians(rot + i * 36 - 90)
        pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return pts

CRESCENT = crescent()
STAR = star(CX + 5.4, CY - 3.4, 2.7, 1.1)

# tassel under the marker bead
TOP = CY + R + MARK_R - 0.4          # ~68.7
CAP = (CX - 2.2, TOP + 0.3, CX + 2.2, TOP + 3.6)
BODY_Y0, BODY_Y1 = TOP + 3.3, 86.0
def tassel_body():
    pts = [(CX - 1.9, BODY_Y0), (CX + 1.9, BODY_Y0)]
    n = 8
    for i in range(n + 1):
        x = CX + 5.6 - i * (11.2 / n)
        y = BODY_Y1 - (0.7 if i % 2 else 0)
        pts.append((x, y))
    # flare: control by inserting intermediate points on both sides
    right = [(CX + 1.9 + (5.6 - 1.9) * (t ** 1.6), BODY_Y0 + (BODY_Y1 - BODY_Y0) * t) for t in np.linspace(0.15, 0.9, 6)]
    left = [(CX - 1.9 - (5.6 - 1.9) * (t ** 1.6), BODY_Y0 + (BODY_Y1 - BODY_Y0) * t) for t in np.linspace(0.9, 0.15, 6)]
    fringe = [(CX + 5.6 - i * (11.2 / n), BODY_Y1 - (0.7 if i % 2 else 0)) for i in range(n + 1)]
    return [(CX - 1.9, BODY_Y0), (CX + 1.9, BODY_Y0)] + right + fringe + left
TASSEL = tassel_body()

def star_outline(cx, cy, outer, ratio=0.765):
    pts = []
    for j in range(16):
        ang = j * math.pi / 8 - math.pi / 2
        r = outer if j % 2 == 0 else outer * ratio
        pts.append((cx + math.cos(ang) * r, cy + math.sin(ang) * r))
    return pts

BG_STARS = [star_outline(CX, CY, r) for r in (26, 40, 54)]

# ---------------------------------------------------------------- Android vector drawables
def f(v): return ("%.2f" % v).rstrip("0").rstrip(".")
def poly_path(pts): return "M" + " L".join(f"{f(x)},{f(y)}" for x, y in pts) + " Z"
def circle_path(x, y, r):
    return f"M{f(x - r)},{f(y)} a{f(r)},{f(r)} 0 1,0 {f(2 * r)},0 a{f(r)},{f(r)} 0 1,0 -{f(2 * r)},0"

HEAD = ('<?xml version="1.0" encoding="utf-8"?>\n'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    xmlns:aapt="http://schemas.android.com/aapt"\n'
        '    android:width="108dp" android:height="108dp"\n'
        '    android:viewportWidth="108" android:viewportHeight="108">\n')

def radial(cx, cy, rad, stops):
    items = "".join(f'<item android:offset="{o}" android:color="{c}"/>' for o, c in stops)
    return (f'<aapt:attr name="android:fillColor"><gradient android:type="radial" android:centerX="{f(cx)}" '
            f'android:centerY="{f(cy)}" android:gradientRadius="{f(rad)}">{items}</gradient></aapt:attr>')

def linear(x0, y0, x1, y1, stops):
    items = "".join(f'<item android:offset="{o}" android:color="{c}"/>' for o, c in stops)
    return (f'<aapt:attr name="android:fillColor"><gradient android:type="linear" android:startX="{f(x0)}" '
            f'android:startY="{f(y0)}" android:endX="{f(x1)}" android:endY="{f(y1)}">{items}</gradient></aapt:attr>')

def background_xml():
    s = HEAD
    s += '    <path android:pathData="M0,0 H108 V108 H0 Z">' + radial(54, 40, 72, [(0, "#1A9478"), (0.5, "#0C5443"), (1, "#06342A")]) + '</path>\n'
    for pts, alpha in zip(BG_STARS, ("26", "1F", "17")):
        s += f'    <path android:pathData="{poly_path(pts)}" android:strokeColor="#{alpha}E6C27A" android:strokeWidth="0.45"/>\n'
    return s + "</vector>\n"

def foreground_xml():
    s = HEAD
    # tassel (behind the marker bead)
    s += f'    <path android:pathData="{poly_path(TASSEL)}">' + linear(CX, BODY_Y0, CX, BODY_Y1, [(0, "#EBCB7E"), (1, "#B8892F")]) + '</path>\n'
    for i in range(7):
        t = i / 6
        x0, x1 = CX - 1.7 + t * 3.4, CX - 5.2 + t * 10.4
        s += f'    <path android:pathData="M{f(x0)},{f(BODY_Y0 + 3)} L{f(x1)},{f(BODY_Y1 - 0.9)}" android:strokeColor="#40000000" android:strokeWidth="0.3"/>\n'
    x0, y0, x1, y1 = CAP
    s += f'    <path android:fillColor="#9C7326" android:pathData="M{f(x0)},{f(y0)} H{f(x1)} V{f(y1)} H{f(x0)} Z"/>\n'
    # beads: soft shadow, gradient body, highlight
    order = [b for b in beads if not b[4]] + [b for b in beads if b[4]]
    for x, y, r, (lt, bs, dk), marker in order:
        s += f'    <path android:fillColor="#38000000" android:pathData="{circle_path(x + 0.4, y + 0.9, r)}"/>\n'
        s += (f'    <path android:pathData="{circle_path(x, y, r)}">'
              + radial(x - 0.35 * r, y - 0.4 * r, 1.5 * r, [(0, hexs(lt)), (0.55, hexs(bs)), (1, hexs(dk))]) + '</path>\n')
        if marker:
            s += f'    <path android:pathData="{circle_path(x, y, r * 0.93)}" android:strokeColor="#E6C27A" android:strokeWidth="0.5"/>\n'
        s += f'    <path android:fillColor="#99FFFFFF" android:pathData="{circle_path(x - 0.38 * r, y - 0.45 * r, 0.2 * r)}"/>\n'
    # crescent and star
    s += f'    <path android:pathData="{poly_path(CRESCENT)}">' + linear(CX - 10, CY - 10, CX + 8, CY + 10, [(0, "#FFE9A6"), (1, "#D9A93F")]) + '</path>\n'
    s += f'    <path android:pathData="{poly_path(STAR)}">' + linear(CX, CY - 7, CX, CY, [(0, "#FFE9A6"), (1, "#D9A93F")]) + '</path>\n'
    return s + "</vector>\n"

def monochrome_xml():
    s = HEAD
    s += f'    <path android:fillColor="#FFFFFFFF" android:pathData="{poly_path(TASSEL)}"/>\n'
    x0, y0, x1, y1 = CAP
    s += f'    <path android:fillColor="#FFFFFFFF" android:pathData="M{f(x0)},{f(y0)} H{f(x1)} V{f(y1)} H{f(x0)} Z"/>\n'
    for x, y, r, _, _ in beads:
        s += f'    <path android:fillColor="#FFFFFFFF" android:pathData="{circle_path(x, y, r * 0.95)}"/>\n'
    s += f'    <path android:fillColor="#FFFFFFFF" android:pathData="{poly_path(CRESCENT)}"/>\n'
    s += f'    <path android:fillColor="#FFFFFFFF" android:pathData="{poly_path(STAR)}"/>\n'
    return s + "</vector>\n"

def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "w").write(text)

write(f"{RES}/drawable/ic_launcher_background.xml", background_xml())
write(f"{RES}/drawable/ic_launcher_foreground.xml", foreground_xml())
write(f"{RES}/drawable/ic_launcher_monochrome.xml", monochrome_xml())
write(f"{RES}/mipmap-anydpi-v26/ic_launcher.xml",
      '<?xml version="1.0" encoding="utf-8"?>\n'
      '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
      '    <background android:drawable="@drawable/ic_launcher_background" />\n'
      '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
      '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
      '</adaptive-icon>\n')

# ---------------------------------------------------------------- PNG rendering (same geometry)
S = 2048
VIEW0, VIEW1 = 18.0, 90.0           # the 72dp window a launcher actually shows
SC = S / (VIEW1 - VIEW0)
def px(x, y): return ((x - VIEW0) * SC, (y - VIEW0) * SC)

def lerp_stops(t, stops):
    t = np.clip(t, 0, 1)
    out = np.zeros(t.shape + (3,), np.float32)
    for i in range(len(stops) - 1):
        (o0, c0), (o1, c1) = stops[i], stops[i + 1]
        m = (t >= o0) & (t <= o1)
        u = ((t - o0) / (o1 - o0))[..., None]
        out = np.where(m[..., None], np.array(c0, np.float32) * (1 - u) + np.array(c1, np.float32) * u, out)
    return out

def render_icon():
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    vx, vy = VIEW0 + xx / SC, VIEW0 + yy / SC
    d = np.sqrt((vx - 54) ** 2 + (vy - 40) ** 2) / 72
    canvas = lerp_stops(d, [(0, (0x1A, 0x94, 0x78)), (0.5, (0x0C, 0x54, 0x43)), (1, (0x06, 0x34, 0x2A))])
    img = Image.fromarray(canvas.astype(np.uint8), "RGB").convert("RGBA")

    # faint star lines
    ov = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    od = ImageDraw.Draw(ov)
    for pts, a in zip(BG_STARS, (38, 31, 23)):
        p = [px(x, y) for x, y in pts]
        od.line(p + [p[0]], fill=(230, 194, 122, a), width=max(2, int(0.5 * SC)), joint="curve")
    img = Image.alpha_composite(img, ov)

    def shadow_layer(draw_fn, blur, alpha):
        m = Image.new("L", (S, S), 0)
        draw_fn(ImageDraw.Draw(m))
        m = m.filter(ImageFilter.GaussianBlur(blur))
        layer = Image.new("RGBA", (S, S), (0, 0, 0, 0))
        layer.putalpha(m.point(lambda v: int(v * alpha)))
        return layer

    # shadows for beads and tassel
    def draw_shadows(dr):
        for x, y, r, _, _ in beads:
            cx_, cy_ = px(x + 0.4, y + 1.0)
            dr.ellipse([cx_ - r * SC, cy_ - r * SC, cx_ + r * SC, cy_ + r * SC], fill=255)
        dr.polygon([px(x + 0.3, y + 0.8) for x, y in TASSEL], fill=255)
    img = Image.alpha_composite(img, shadow_layer(draw_shadows, 0.9 * SC, 0.45))

    arr = np.array(img).astype(np.float32)[..., :3]

    def paint_mask(mask, color):
        nonlocal arr
        m = mask[..., None]
        arr = arr * (1 - m) + color * m

    def poly_mask(pts):
        m = Image.new("L", (S, S), 0)
        ImageDraw.Draw(m).polygon([px(x, y) for x, y in pts], fill=255)
        return np.array(m, np.float32) / 255

    # tassel
    tm = poly_mask(TASSEL)
    tcol = lerp_stops((vy - BODY_Y0) / (BODY_Y1 - BODY_Y0), [(0, (0xEB, 0xCB, 0x7E)), (1, (0xB8, 0x89, 0x2F))])
    paint_mask(tm, tcol)
    sl = Image.new("L", (S, S), 0)
    sd = ImageDraw.Draw(sl)
    for i in range(7):
        t = i / 6
        sd.line([px(CX - 1.7 + t * 3.4, BODY_Y0 + 3), px(CX - 5.2 + t * 10.4, BODY_Y1 - 0.9)], fill=255, width=max(2, int(0.3 * SC)))
    sm = np.array(sl.filter(ImageFilter.GaussianBlur(1.2)), np.float32) / 255 * tm * 0.25
    paint_mask(sm, np.array([0, 0, 0], np.float32))
    cm = Image.new("L", (S, S), 0)
    x0, y0, x1, y1 = CAP
    ImageDraw.Draw(cm).rounded_rectangle([px(x0, y0), px(x1, y1)], radius=int(0.8 * SC), fill=255)
    paint_mask(np.array(cm, np.float32) / 255, np.array([0x9C, 0x73, 0x26], np.float32))

    def bead(x, y, r, light, base, dark, marker):
        nonlocal arr
        cx_, cy_ = px(x, y)
        rp = r * SC
        m = int(rp * 1.6)
        x0_, x1_ = int(cx_ - m), int(cx_ + m)
        y0_, y1_ = int(cy_ - m), int(cy_ + m)
        gy, gx = np.mgrid[y0_:y1_, x0_:x1_].astype(np.float32)
        dist = np.sqrt((gx - cx_) ** 2 + (gy - cy_) ** 2)
        mask = np.clip(rp - dist + 0.5, 0, 1)
        hd = np.sqrt((gx - (cx_ - 0.35 * rp)) ** 2 + (gy - (cy_ - 0.4 * rp)) ** 2) / (1.5 * rp)
        col = lerp_stops(hd, [(0, light), (0.55, base), (1.0, dark)])
        rim = np.exp(-(((gx - (cx_ + 0.62 * rp)) ** 2 + (gy - (cy_ + 0.66 * rp)) ** 2) / (0.5 * rp) ** 2))[..., None]
        col = col * (1 - 0.22 * rim) + 255 * 0.22 * rim
        edge = np.clip((dist - 0.93 * rp) / (0.07 * rp), 0, 1)[..., None]
        col = col * (1 - 0.25 * edge)
        if marker:
            ring = np.clip(1 - np.abs(dist - 0.9 * rp) / (0.04 * rp), 0, 1)[..., None] * 0.9
            col = col * (1 - ring) + np.array([230, 194, 122], np.float32) * ring
        sp = np.exp(-(((gx - (cx_ - 0.38 * rp)) / (0.28 * rp)) ** 2 + ((gy - (cy_ - 0.45 * rp)) / (0.17 * rp)) ** 2))[..., None] * 0.65
        col = col * (1 - sp) + 255 * sp
        patch = arr[y0_:y1_, x0_:x1_]
        arr[y0_:y1_, x0_:x1_] = patch * (1 - mask[..., None]) + col * mask[..., None]

    for x, y, r, (lt, bs, dk), mk in [b for b in beads if not b[4]] + [b for b in beads if b[4]]:
        bead(x, y, r, lt, bs, dk, mk)

    # crescent + star in gold
    gold = lerp_stops(((vx - (CX - 10)) * 18 + (vy - (CY - 10)) * 20) / (18 * 18 + 20 * 20), [(0, (0xFF, 0xE9, 0xA6)), (1, (0xD9, 0xA9, 0x3F))])
    for pts in (CRESCENT, STAR):
        paint_mask(poly_mask(pts), gold)

    out = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGB")
    return out.resize((512, 512), Image.LANCZOS)

os.makedirs(OUT, exist_ok=True)
icon = render_icon()
icon.save(f"{OUT}/icon_512.png")

# ---------------------------------------------------------------- feature graphic 1024 x 500
def feature():
    W, H = 1024, 500
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
    d = np.sqrt(((xx - 300) / 700) ** 2 + ((yy - 220) / 420) ** 2)
    bg = lerp_stops(d, [(0, (0x1A, 0x94, 0x78)), (0.5, (0x0C, 0x54, 0x43)), (1, (0x06, 0x34, 0x2A))])
    img = Image.fromarray(bg.astype(np.uint8), "RGB").convert("RGBA")
    ov = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    od = ImageDraw.Draw(ov)
    for r, a in ((190, 40), (300, 30), (410, 22), (520, 16)):
        pts = []
        for j in range(16):
            ang = j * math.pi / 8 - math.pi / 2
            rr = r if j % 2 == 0 else r * 0.765
            pts.append((300 + math.cos(ang) * rr, 250 + math.sin(ang) * rr))
        od.line(pts + [pts[0]], fill=(230, 194, 122, a), width=2)
    img = Image.alpha_composite(img, ov)

    ic = icon.resize((330, 330), Image.LANCZOS).convert("RGBA")
    mask = Image.new("L", (330, 330), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, 329, 329], radius=74, fill=255)
    shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sm = Image.new("L", (W, H), 0)
    ImageDraw.Draw(sm).rounded_rectangle([90, 100, 420, 430], radius=74, fill=150)
    shadow.putalpha(sm.filter(ImageFilter.GaussianBlur(16)))
    img = Image.alpha_composite(img, shadow)
    img.paste(ic, (90, 85), mask)

    dr = ImageDraw.Draw(img)
    title = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf", 108)
    sub = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf", 29)
    small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 22)
    dr.text((470, 150), "Tasbih", font=title, fill=(246, 239, 221))
    dr.text((474, 288), "A calm place for your dhikr", font=sub, fill=(230, 194, 122))
    dr.text((474, 346), "Real beads  ·  Daily progress  ·  Offline", font=small, fill=(190, 214, 205))
    return img.convert("RGB")

feature().save(f"{OUT}/feature_graphic_1024x500.png")
print("icon + feature graphic + vector drawables written")
