/*
 * This file is part of ***  M y C o R e  ***
 * See https://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.jspdocportal.common.xsl;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;

import org.mycore.common.MCRCache;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xsl.MCRTemplatesSource;

/**
 * Variant of {@link MCRXSLTransformer} that allows the {@link MCRTemplatesSource}
 * instances to be supplied directly to the constructor instead of being resolved
 * through the standard lookup mechanism
 * <p>
 * Instances should generally be obtained via {@link #obtainInstance}, which caches
 * instances by factory class and template source keys, so that stylesheets are not
 * needlessly recompiled on every call.
 */
public class MCRDirectTemplatesSourceTransformer extends MCRXSLTransformer {

    private static final MCRCache<String, MCRDirectTemplatesSourceTransformer> INSTANCE_CACHE =
        new MCRCache<>(100L, "MCRDirectTemplatesSourceTransformer instance cache");

    /**
     * Creates a transformer for the given, already resolved template sources.
     *
     * @param factoryClass the {@link TransformerFactory} implementation to use for compiling the stylesheets
     * @param templateSources the template sources to compile and cache, in the order they should be applied
     */
    public MCRDirectTemplatesSourceTransformer(Class<? extends TransformerFactory> factoryClass,
        MCRTemplatesSource... templateSources) {
        super(factoryClass);
        this.templateSources = templateSources;
        this.modified = new long[this.templateSources.length];
        this.modifiedChecked = 0L;
        this.templates = new Templates[this.templateSources.length];
    }

    /**
     * Returns a cached instance for the given factory class and template sources,
     * creating and caching a new one if none exists yet.
     * <p>
     * The cache key is derived from {@code factoryClass} and the
     * {@link MCRTemplatesSource#getKey()} of each given template source, so repeated
     * calls with equivalent sources (e.g. the same virtual stylesheet) reuse the same
     * transformer instance instead of recompiling the stylesheet on every call.
     *
     * @param factoryClass the {@link TransformerFactory} implementation to use for compiling the stylesheets
     * @param templateSources the template sources to compile and cache, in the order they should be applied
     * @return a cached or newly created transformer for the given sources
     */
    public static synchronized MCRDirectTemplatesSourceTransformer obtainInstance(
        Class<? extends TransformerFactory> factoryClass, MCRTemplatesSource... templateSources) {
        String key = factoryClass.getName() + "_"
            + Arrays.stream(templateSources).map(MCRTemplatesSource::getKey).collect(Collectors.joining(","));
        MCRDirectTemplatesSourceTransformer instance = INSTANCE_CACHE.get(key);
        if (instance == null) {
            instance = new MCRDirectTemplatesSourceTransformer(factoryClass, templateSources);
            INSTANCE_CACHE.put(key, instance);
        }
        return instance;
    }
}
