"""
Python mirror of app/src/main/java/com/tasbih/app/ui/SoundSynth.kt, so you can listen to the bead sounds on a
computer (and check them numerically) before building the app.

Run:  python3 tools/sound_preview.py      -> play_store/sound_previews/*.wav
"""
import math, os, random, wave
import numpy as np

RATE = 44100
OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "play_store", "sound_previews")

def voice(style, rnd):
    j = 1.0 + (rnd.random() * 2 - 1) * 0.05
    if style == "Soft":
        return dict(modes=[(1.0, 1.0, 300.0), (1.83, 0.45, 430.0), (2.9, 0.22, 600.0)], base=1850.0 * j,
                    body_hz=380.0 * j, body_gain=0.28, body_decay=150.0,
                    noise=0.55, noise_decay=950.0, bandpass=True, length_ms=46, soften=0.55)
    return dict(modes=[(1.0, 1.0, 120.0), (2.15, 0.55, 180.0), (3.6, 0.25, 270.0)], base=760.0 * j,
                body_hz=250.0 * j, body_gain=0.4, body_decay=95.0,
                noise=0.45, noise_decay=700.0, bandpass=False, length_ms=85, soften=0.8)

HITS = {
    "TICK": [(0, 1.0, 1.0)],
    "MARKER": [(0, 0.78, 1.0), (40, 0.78, 0.6)],
    "COMPLETE": [(0, 0.7, 1.0), (95, 0.7, 0.75)],
}
PEAK = {"TICK": 0.5, "MARKER": 0.6, "COMPLETE": 0.6}

def render(style, event, seed):
    rnd = random.Random(seed)
    v = voice(style, rnd)
    hits = HITS[event]
    total_ms = hits[-1][0] + v["length_ms"]
    n = RATE * total_ms // 1000
    out = np.zeros(n)
    for at_ms, pitch, amp in hits:
        start = RATE * at_ms // 1000
        l1 = l2 = 0.0
        for i in range(RATE * v["length_ms"] // 1000):
            if start + i >= n: break
            t = i / RATE
            s = 0.0
            for ratio, a, decay in v["modes"]:
                s += a * math.sin(2 * math.pi * v["base"] * pitch * ratio * t) * math.exp(-decay * t)
            s += v["body_gain"] * math.sin(2 * math.pi * v["body_hz"] * pitch * t) * math.exp(-v["body_decay"] * t)
            r = rnd.random() * 2 - 1
            if v["bandpass"]:
                l1 += 0.7 * (r - l1); l2 += 0.15 * (r - l2); noise = l1 - l2
            else:
                l1 += 0.5 * (r - l1); noise = l1
            s += noise * v["noise"] * math.exp(-t * v["noise_decay"])
            out[start + i] += s * amp * min(1.0, t / 0.0006)
    y = 0.0
    a = v["soften"]
    for i in range(n):
        y += a * (out[i] - y)
        out[i] = y
    peak = max(1e-9, float(np.max(np.abs(out))))
    out *= PEAK[event] / peak
    fade = RATE * 3 // 1000
    out[n - fade:] *= np.linspace(1, 0, fade)
    return out

def save(name, data):
    os.makedirs(OUT, exist_ok=True)
    pcm = (np.clip(data, -1, 1) * 32767).astype("<i2")
    with wave.open(os.path.join(OUT, name), "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(RATE); w.writeframes(pcm.tobytes())

def analyse(data):
    env = np.abs(data)
    peak = env.max()
    above = np.where(env > peak * 0.01)[0]       # -40 dB
    dur_ms = (above[-1] + 1) / RATE * 1000
    spec = np.abs(np.fft.rfft(data * np.hanning(len(data)), 8192))
    freqs = np.fft.rfftfreq(8192, 1 / RATE)
    centroid = float((spec * freqs).sum() / spec.sum())
    rms = float(np.sqrt(np.mean(data ** 2)))
    return peak, dur_ms, centroid, rms

if __name__ == "__main__":
    for style in ("Soft", "Wood"):
        for event in ("TICK", "MARKER", "COMPLETE"):
            d = render(style, event, 1)
            peak, dur, cen, rms = analyse(d)
            print(f"{style:5s} {event:9s} peak={peak:.2f}  audible={dur:5.0f} ms  centroid={cen:5.0f} Hz  rms={rms:.3f}")
            save(f"{style.lower()}_{event.lower()}.wav", d)
        # a short run of ten beads, like counting quickly
        run = np.concatenate([np.concatenate([render(style, "TICK", s), np.zeros(int(RATE * 0.14))]) for s in range(1, 11)])
        save(f"{style.lower()}_ten_beads.wav", run)
    print("wrote", OUT)
