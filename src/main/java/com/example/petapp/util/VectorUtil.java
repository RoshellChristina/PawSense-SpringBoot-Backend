package com.example.petapp.util;

import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class
VectorUtil {

    /** Convert boxed Double[] to Postgres vector literal "[v1,v2,...]" */
    public static String toVectorLiteral(Double[] v) {
        if (v == null) return null;
        StringBuilder sb = new StringBuilder(v.length * 6);
        sb.append('[');
        for (int i = 0; i < v.length; ++i) {
            if (i > 0) sb.append(',');
            // handle nulls defensively
            sb.append(v[i] == null ? "0.0" : v[i].toString());
        }
        sb.append(']');
        return sb.toString();
    }

}
