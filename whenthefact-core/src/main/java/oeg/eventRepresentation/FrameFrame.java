/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oeg.eventRepresentation;

import java.util.ArrayList;

/**
 *
 * @author mnavas
 */
public class FrameFrame implements java.io.Serializable {
    
    public String name;
    public String core;
    public String pos;

    @Override
    public String toString() {
        return "Frame{" + "core=" + core + ", name=" + name + ", pos=" + pos + '}' + '\n';
    }

    
}
