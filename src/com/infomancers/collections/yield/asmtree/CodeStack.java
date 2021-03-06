package com.infomancers.collections.yield.asmtree;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public final class CodeStack {

    private static final char stackDiff = 'F';

    private static final String stacks =
            // 0123456789
            "FGGGGGGGGG" + // 0
                    "GGGGGGGGGG" + // 1
                    "GGGGGGGGGG" + // 2
                    "GGGGGGGGGG" + // 3
                    "GGGGGGEEEE" + // 4
                    "EEEEEEEEEE" + // 5
                    "EEEEEEEEEE" + // 6
                    "EEEEEEEEEC" + // 7
                    "CCCCCCCEDG" + // 8
                    "GGHHHFEEEE" + // 9
                    "EEEEEEEEEE" + // 10
                    "EEEEEEFFFF" + // 11
                    "EEEEEEEEEE" + // 12
                    "EEFFFFFFFF" + // 13
                    "FFFFFFFFEE" + // 14
                    "EEEEEEEEED" + // 15
                    "DDDDDDDFGF" + // 16
                    "EEZZZZZZGE" + // 17
                    "FDZZZZZGFF" + // 18
                    "FZFFEEZZEE" + // 19
                    "FF";          // 20


    public static boolean changeStack(AbstractInsnNode node) {
        return !(node.getOpcode() == Opcodes.CHECKCAST ||
                node.getType() == AbstractInsnNode.LABEL);
    }

    public static int getChange(AbstractInsnNode node) {
        switch (node.getOpcode()) {
            case -1:
                return 0;

            case Opcodes.INVOKEINTERFACE:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEVIRTUAL: {
                int result = -1;
                MethodInsnNode method = (MethodInsnNode) node;

                if (!method.desc.contains("V")) {
                    result++;
                }

                result -= countParams(method.desc);

                return result;
            }
            case Opcodes.INVOKESTATIC: {
                int result = 0;
                MethodInsnNode method = (MethodInsnNode) node;

                if (!method.desc.contains("V")) {
                    result++;
                }

                result -= countParams(method.desc);

                return result;
            }
            case Opcodes.MULTIANEWARRAY:
                MultiANewArrayInsnNode multiANewArrayInsnNode = (MultiANewArrayInsnNode) node;
                return -multiANewArrayInsnNode.dims;

            case Opcodes.RETURN:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
                throw new IllegalStateException("Shouldn't be following opcode " + node.getOpcode());
            default:
                return stacks.charAt(node.getOpcode()) - stackDiff;
        }
    }

    private static int countParams(String desc) {
        final Pattern params = Pattern.compile("[BCDFIJSZ]|L[\\w\\d_]+(?:/[\\w\\d_]+)*;");
        int counter = 0;
        Matcher m = params.matcher(desc.split("\\)")[0]);
        while (m.find()) {
            counter++;
        }

        return counter;
    }

    private static boolean isJump(AbstractInsnNode node) {
        return node != null && (node.getType() == AbstractInsnNode.JUMP_INSN ||
                node.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN ||
                node.getType() == AbstractInsnNode.TABLESWITCH_INSN);

    }

    public static AbstractInsnNode backUntilStackSizedAt(AbstractInsnNode start, final int requiredSize, final boolean followNoStackChangers, final List<AbstractInsnNode> limits) {
        int stackSize = 0;
        AbstractInsnNode backNode = start;
        do {
            stackSize += getChange(backNode);
            backNode = backNode.getPrevious();
        } while (backNode != null && !isJump(backNode) && !limits.contains(backNode) && stackSize != requiredSize);

        // continue if there are no-stack-changers before this command
        if (followNoStackChangers) {
            while (backNode != null && !isJump(backNode) && !limits.contains(backNode) && getChange(backNode) == 0) {
                backNode = backNode.getPrevious();
            }
        }

        // don't create a situation where the new opcode is right before a label; this would probably be a mistake.
        while (backNode != null && backNode.getNext().getType() == AbstractInsnNode.LABEL) {
            backNode = backNode.getNext();
        }

        return backNode;
    }
}
