#!/usr/bin/env python3
"""Comprobación estática sin SDK; NO sustituye la compilación."""
from pathlib import Path
import xml.etree.ElementTree as ET
root = Path(__file__).resolve().parent
ET.parse(root / 'app/src/main/AndroidManifest.xml')
for xml in (root / 'app/src/main/res/values').glob('*.xml'):
    ET.parse(xml)
for xml in (root / 'app/src/main/res/drawable').glob('*.xml'):
    ET.parse(xml)
required = ['app/build.gradle.kts', 'settings.gradle.kts', 'app/src/main/java/es/cleantv/tv/MainActivity.kt', '.github/workflows/build-apk.yml']
for path in required:
    assert (root/path).is_file(), path
print('OK: recursos XML legibles y estructura mínima presente. COMPILACIÓN NO VERIFICADA.')
