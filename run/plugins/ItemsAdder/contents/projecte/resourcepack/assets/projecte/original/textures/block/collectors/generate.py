from pathlib import Path
from shutil import copy2

if __name__ == "__main__":
    src = Path("./other.png")
    for i in range(1, 4):
        for ii in ("south", "east", "west", "down"):
            copy2(src, Path(f"collector_{i}_{ii}.png"))

    src = Path("./front.png")
    for i in range(1, 4):
        copy2(src, Path(f"collector_{i}_north.png"))

    copy2(Path("./top_1.png"), Path(f"collector_1_up.png"))
    copy2(Path("./top_2.png"), Path(f"collector_2_up.png"))
    copy2(Path("./top_3.png"), Path(f"collector_3_up.png"))

    