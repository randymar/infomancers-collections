package com.infomancers.collections.yield.asm.promotion;

import com.infomancers.collections.yield.asm.TypeDescriptor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Copyright (c) 2007, Aviad Ben Dov
 * <p/>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 3. Neither the name of Infomancers, Ltd. nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific
 * prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
final class SimpleArrayAccessorCreator implements ArrayAccessorCreator {
    /**
     * Sets a value into an array.
     * <p/>
     * The stack is already filled with [..., arr, index, value ].
     * <p/>
     * The stack should be [ ... ] at the end.
     *
     * @param mv   The method visitor to write the code through.
     * @param type The type of the array.
     */
    public void createSetValueCode(MethodVisitor mv, TypeDescriptor type) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/reflect/Array",
                type.getArraySetterMethodName(), type.getArraySetterMethodDesc());
    }

    /**
     * Gets a value from an array.
     * <p/>
     * The stack is already filled with [..., arr, index ].
     * <p/>
     * The should be [ ..., value ] at the end.
     *
     * @param mv   The method visitor to write the code through.
     * @param type The type of the array.
     */
    public void createGetValueCode(MethodVisitor mv, TypeDescriptor type) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/reflect/Array",
                type.getArrayGetterMethodName(), type.getArrayGetterMethodDesc());
    }
}
