package de.cristelknight.cristellib.neoforge.extraapiutil;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.Constants;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;


public class APIFinder {


    @SuppressWarnings("unchecked")
    public static <T> List<Pair<List<String>, T>> scanForAPIs(Class<?> annotationClazz, Class<T> returnClazz) {
        List<Pair<List<String>, T>> instances = new ArrayList<>();
        for (ModFileScanData data : ModList.get().getAllScanData()) {
            List<ModFileScanData.AnnotationData> ebsTargets = data.getAnnotations().stream().
                    filter(annotationData -> Type.getType(annotationClazz).equals(annotationData.annotationType())).
                    toList();

            List<String> modIds = data.getIModInfoData().stream()
                    .flatMap(info -> info.getMods().stream())
                    .map(IModInfo::getModId)
                    .toList();

            for(ModFileScanData.AnnotationData ad : ebsTargets){
                Class<T> clazz;

                try {
                    Class<?> clazz2 = Class.forName(ad.memberName());
                    if(!returnClazz.isAssignableFrom(clazz2)) {
                        Constants.LOG.error("Failed to load api class {} for @{} annotation", ad.clazz().getClassName(), annotationClazz.getSimpleName());
                        continue;
                    }
                    clazz = (Class<T>) clazz2;
                } catch (ClassNotFoundException e) {
                    Constants.LOG.error("Failed to load api class {} for @{} annotation", ad.clazz().getClassName(), annotationClazz.getSimpleName(), e);
                    continue;
                }
                try {
                    instances.add(new Pair<>(modIds, clazz.getDeclaredConstructor().newInstance()));
                } catch (Throwable throwable) {
                    Constants.LOG.error("Failed to load api: {}", ad.memberName(), throwable);
                }
            }
        }
        return instances;
    }
}