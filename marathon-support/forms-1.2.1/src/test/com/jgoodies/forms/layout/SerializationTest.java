/*
 * Copyright (c) 2002-2008 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.forms.layout;

import java.io.*;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of instances of
 * <code>FormLayout</code> and <code>JPanel</code>.
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.8 $
 *
 * @see java.io.Serializable
 */
public final class SerializationTest extends TestCase {


    /**
     * Tests the serialization of a FormLayout that has just been constructed.
     * The layout contains no components and the layout algorithm has not
     * been performed.
     */
    public void testSerializeConstructedLayout() {
        FormLayout layout = createSampleLayout();
        OutputStream out = new ByteArrayOutputStream();
        serialize(out, layout);
    }


    /**
     * Tests the serialization of an empty FormLayout that has computed
     * a layout immediately after its construction.
     * The layout contains no components.
     */
    public void testSerializeEmptyLayout() {
        FormLayout layout = createSampleLayout();
        doLayout(layout);
        OutputStream out = new ByteArrayOutputStream();
        serialize(out, layout);
    }


    /**
     * Tests the serialization of a panel that is laid out using
     * a FormLayout. The panel consists some sample components that in turn
     * uses some sample <code>CellConstraints</code>.
     */
    public void testSerializePanel() {
        JPanel panel = createSamplePanel();
        OutputStream out = new ByteArrayOutputStream();
        serialize(out, panel);
    }


    /**
     * Tests the deserialization of a FormLayout that has just been constructed.
     * The layout contains no components and the layout algorithm has not
     * been performed.
     */
    public void testDeserializeConstructedLayout() {
        FormLayout layout = createSampleLayout();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialize(out, layout);
        byte[] bytes = out.toByteArray();
        InputStream in = new ByteArrayInputStream(bytes);
        deserialize(in);
    }


    /**
     * Tests the deserialization of an empty FormLayout that has computed
     * a layout immediately after its construction.
     * The layout contains no components.
     */
    public void testDeserializeEmptyLayout() {
        FormLayout layout = createSampleLayout();
        doLayout(layout);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialize(out, layout);
        byte[] bytes = out.toByteArray();
        InputStream in = new ByteArrayInputStream(bytes);
        deserialize(in);
    }


    /**
     * Tests the deserialization of a panel that is laid out using
     * a FormLayout. The panel consists some sample components that in turn
     * uses some sample <code>CellConstraints</code>.
     */
    public void testDeserializePanel() {
        JPanel panel = createSamplePanel();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialize(out, panel);
        byte[] bytes = out.toByteArray();
        InputStream in = new ByteArrayInputStream(bytes);
        deserialize(in);
    }


    /**
     * Tests that the a layout can be computed with a deserialized
     * empty FormLayout.
     */
    public void testLayoutDeserializedEmptyLayout() {
        FormLayout layout = createSampleLayout();
        doLayout(layout);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialize(out, layout);
        byte[] bytes = out.toByteArray();
        InputStream in = new ByteArrayInputStream(bytes);
        FormLayout layout2 = (FormLayout) deserialize(in);
        doLayout(layout2);
    }


    /**
     * Tests that the a panel can be laid out with a deserialized
     * FormLayout.
     */
    public void testLayoutDeserializedPanel() {
        JPanel panel = createSamplePanel();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialize(out, panel);
        byte[] bytes = out.toByteArray();
        InputStream in = new ByteArrayInputStream(bytes);
        JPanel panel2 = (JPanel) deserialize(in);
        panel2.doLayout();
    }


    // Helper Code *********************************************************

    /**
     * Creates and returns a sample <code>FormLayout</code> instance
     * that uses all prebuilt alignments and <code>Size</code> implementations
     * so we can test their serialization and deserialization.
     *
     * @return a sample layout
     */
    private FormLayout createSampleLayout() {
        return new FormLayout(
                "l:1px, c:2dlu, r:3mm, f:m, p, d, max(p;3dlu), min(p;7px)",
                "t:1px, c:2dlu, b:3mm, f:m, p, d, max(p;3dlu), min(p;7px)");
    }


    /**
     * Creates and returns a sample panel that uses the sample layout
     * created by <code>#createSampleLayout</code>. Useful to test the
     * FormLayout serialization in combination with a layout container
     * and some components managed by the FormLayout. Especially it tests
     * the serialization of <code>CellConstraints</code> objects.
     *
     * @return a sample panel
     */
    private JPanel createSamplePanel() {
        JPanel panel = new JPanel(createSampleLayout());
        CellConstraints cc = new CellConstraints();
        panel.add(new JLabel("Test1"),  cc.xy(1, 1, "l, t"));
        panel.add(new JButton("Test2"), cc.xy(2, 2, "c, c"));
        panel.add(new JButton("Test3"), cc.xy(3, 3, "r, b"));
        panel.add(new JButton("Test4"), cc.xy(4, 4, "f, f"));
        panel.doLayout();
        return panel;
    }


    /**
     * Lays out a container using the given <code>FormLayout</code>
     * and returns the layout info object.
     *
     * @param layout    the FormLayout used to lay out
     * @return the layout info after the container has been laid out
     */
    private FormLayout.LayoutInfo doLayout(FormLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.doLayout();
        FormLayout.LayoutInfo info = layout.getLayoutInfo(panel);
        return info;
    }


    /**
     * Serializes the given object and writes it to the given
     * output stream.
     *
     * @param out      the stream to write the serialized object
     * @param object   the object to be serialized
     */
    private void serialize(OutputStream out, Object object) {
        ObjectOutputStream objectOut = null;
        try {
            objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
            fail("IO Exception");
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    /**
     * Deserializes and returns an object that is contained in serialized form
     * in the given input stream.
     *
     * @param in   the stream to read from
     * @return the deserialized object
     */
    private Object deserialize(InputStream in) {
        ObjectInputStream objectIn = null;
        try {
            objectIn = new ObjectInputStream(in);
            return objectIn.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            fail("IO Exception");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            fail("Class not found");
        } finally {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }


}
