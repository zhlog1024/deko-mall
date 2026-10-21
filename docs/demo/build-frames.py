#!/usr/bin/env python3
"""Normalize screenshots into 1920x1080 frames, grouped by end."""
from pathlib import Path

import numpy as np
from PIL import Image

shots = Path("/var/folders/32/wbqgc0c15j5f21g1qwmrljg00000gn/T/cursor/screenshots")
out = Path(__file__).resolve().parent / "frames"
out.mkdir(parents=True, exist_ok=True)

W, H = 1920, 1080
BG = (245, 247, 251)
# H5 phone sits on dark browser chrome — crop that away then center
PHONE_BG = (245, 247, 251)


def to_opaque_rgb(im: Image.Image, bg=BG) -> Image.Image:
    if im.mode in ("RGBA", "LA") or (im.mode == "P" and "transparency" in im.info):
        base = Image.new("RGBA", im.size, (*bg, 255))
        base.paste(im.convert("RGBA"), mask=im.convert("RGBA").split()[-1])
        return base.convert("RGB")
    return im.convert("RGB")


def fit_cover_canvas(im: Image.Image) -> Image.Image:
    im = to_opaque_rgb(im)
    scale = max(W / im.width, H / im.height)
    nw, nh = int(im.width * scale), int(im.height * scale)
    im = im.resize((nw, nh), Image.Resampling.LANCZOS)
    left = (nw - W) // 2
    top = (nh - H) // 2
    return im.crop((left, top, left + W, top + H))


def extract_phone(im: Image.Image) -> Image.Image:
    """Crop H5 content off dark letterbox, return phone RGB."""
    raw = to_opaque_rgb(im, bg=(32, 32, 32))
    arr = np.array(raw)
    # treat near-black / near-#333 chrome as empty
    empty = (arr.max(axis=2) < 40) | (np.abs(arr.astype(int) - 51).max(axis=2) < 12)
    ys, xs = np.where(~empty)
    if len(xs) == 0:
        return raw
    return raw.crop((int(xs.min()), int(ys.min()), int(xs.max()) + 1, int(ys.max()) + 1))


def fit_contain_canvas(im: Image.Image) -> Image.Image:
    im = to_opaque_rgb(im, bg=PHONE_BG)
    canvas = Image.new("RGB", (W, H), PHONE_BG)
    copy = im.copy()
    copy.thumbnail((int(W * 0.34), int(H * 0.94)), Image.Resampling.LANCZOS)
    x = (W - copy.width) // 2
    y = (H - copy.height) // 2
    canvas.paste(copy, (x, y))
    return canvas


# dest_name -> source filename in shots/
DESKTOP = {
    # admin
    "admin-01-login.png": "01-login.png",
    "admin-02-filled.png": "02-filled.png",
    "admin-03-dashboard.png": "03-dashboard.png",
    "admin-04-system.png": "admin-system-user.png",
    "admin-05-app-config.png": "admin-app-config.png",
    "admin-06-diy-list.png": "admin-diy-list.png",
    "admin-07-diy-design.png": "admin-diy-design.png",
    # scm
    "scm-01-login.png": "04-scm-login.png",
    "scm-02-filled.png": "05-scm-filled.png",
    "scm-03-dashboard.png": "scm-dashboard.png",
    "scm-04-goods.png": "scm-goods.png",
    "scm-05-orders.png": "scm-orders.png",
}

PHONE = {
    "app-01-home.png": "app-home-raw.png",
    "app-02-category.png": "app-category-raw.png",
    "app-03-detail.png": "app-detail-raw.png",
    "app-04-buy.png": "app-buy-raw.png",
    "app-05-user.png": "app-user-raw.png",
}


def main() -> None:
    keep = set()
    for dest, src_name in DESKTOP.items():
        src = shots / src_name
        if not src.exists():
            raise SystemExit(f"missing {src}")
        fit_cover_canvas(Image.open(src)).save(out / dest, optimize=True)
        keep.add(dest)
        print("saved", dest)

    for dest, src_name in PHONE.items():
        src = shots / src_name
        if not src.exists():
            raise SystemExit(f"missing {src}")
        phone = extract_phone(Image.open(src))
        fit_contain_canvas(phone).save(out / dest, optimize=True)
        keep.add(dest)
        print("saved", dest, "phone", phone.size)

    for stale in out.glob("*.png"):
        if stale.name not in keep:
            stale.unlink()
            print("removed", stale.name)

    for p in sorted(out.glob("*.png")):
        im = Image.open(p)
        print(p.name, im.size, im.mode)


if __name__ == "__main__":
    main()
