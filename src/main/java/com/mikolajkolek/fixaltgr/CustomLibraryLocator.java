package com.mikolajkolek.fixaltgr;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeLibraryLocator;
import com.github.kwhat.jnativehook.NativeSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CustomLibraryLocator implements NativeLibraryLocator {
    public static void setAaDefaultLocator() {
        System.setProperty("jnativehook.lib.locator", CustomLibraryLocator.class.getCanonicalName());
    }

    @Override
    public Iterator<File> getLibraries() {
        List<File> libraries = new ArrayList<>(1);

        String libName = System.getProperty("jnativehook.lib.name", "JNativeHook");

        // Get the package name for GlobalScreen
        String basePackage = GlobalScreen.class.getPackage().getName().replace('.', '/');

        String libNativeArch = NativeSystem.getArchitecture().toString().toLowerCase(Locale.ROOT);
        String libNativeName = System
                .mapLibraryName(libName)
                .replaceAll("\\.jnilib$", "\\.dylib");

        // Resource path for native library
        String libResourcePath = "/" + basePackage + "/lib/" +
                NativeSystem.getFamily().toString().toLowerCase(Locale.ROOT) +
                '/' + libNativeArch + '/' + libNativeName;

        File libDir;
        String customLibPath = System.getProperty("jnativehook.lib.path");
        if (customLibPath != null && !customLibPath.trim().isEmpty()) {
            libDir = new File(customLibPath);
        } else {
            libDir = new File(System.getProperty("java.io.tmpdir"), "jnativehook-2.2.2");
        }
        libDir.mkdirs();

        File libFile = new File(libDir, libNativeName);

        if (!libFile.exists() || libFile.length() == 0) {
            InputStream resourceInputStream = GlobalScreen.class.getResourceAsStream(libResourcePath);
            if (resourceInputStream == null) {
                resourceInputStream = FixAltGr.class.getResourceAsStream(libResourcePath);
            }
            if (resourceInputStream == null) {
                resourceInputStream = CustomLibraryLocator.class.getClassLoader().getResourceAsStream(libResourcePath.startsWith("/") ? libResourcePath.substring(1) : libResourcePath);
            }
            if (resourceInputStream == null) {
                throw new RuntimeException("Unable to extract the native library " + libResourcePath + "!\n");
            }

            try (InputStream in = resourceInputStream;
                 FileOutputStream out = new FileOutputStream(libFile)) {
                byte[] buffer = new byte[8192];
                int size;
                while ((size = in.read(buffer)) != -1) {
                    out.write(buffer, 0, size);
                }
                FixAltGr.LOGGER.info("Extracted native library: " + libFile.getAbsolutePath());
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract native library: " + e.getMessage(), e);
            }
        }

        FixAltGr.LOGGER.info("Loading native library: " + libFile.getAbsolutePath());
        libraries.add(libFile);

        return libraries.iterator();
    }
}
