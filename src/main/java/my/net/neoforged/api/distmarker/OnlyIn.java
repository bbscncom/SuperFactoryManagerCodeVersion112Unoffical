/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package my.net.neoforged.api.distmarker;

import java.lang.annotation.*;


/**
 * Marks the associated element as being only available on a certain {@link net.neoforged.api.distmarker.Dist}.
 *
 * Classes, fields, methods and constructors can be marked as only available in a specific distribution
 * based on the presence of this annotation.
 *
 *
 * <p>This is generally meant for internal Forge and FML use only
 * and modders should avoid its use whenever possible.</p>
 *
 *
 * Note, this will <em>only</em> apply to the direct element marked. This code:
 * {@code @OnlyIn(Dist.CLIENT) public MyField field = new MyField();} will <strong>not</strong> work,
 * as the initializer is a separate piece of code to the actual field declaration, and will not be able to find
 * it's field on the wrong side.
 *
 * When applied on a package, this only applies to the package class file itself.
 * It is a reasonable assumption that the whole package will also be restricted to that distribution, but not a requirement.
 *
 * When applied on an annotation, this only applies to the annotation class itself, not any members that are annotated with it.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE, ElementType.ANNOTATION_TYPE})
@Repeatable(OnlyIns.class)
public @interface OnlyIn
{
    public Dist value();
    public Class<?> _interface() default Object.class;
}
