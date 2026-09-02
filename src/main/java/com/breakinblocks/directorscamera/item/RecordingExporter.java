package com.breakinblocks.directorscamera.item;

import com.breakinblocks.directorscamera.cutscene.CameraPos;
import com.breakinblocks.directorscamera.cutscene.CutsceneJsonWriter;

import java.util.List;
import java.util.Locale;

public final class RecordingExporter {
    private RecordingExporter() {
    }

    public static String toScript(CameraRecording recording) {
        StringBuilder sb = new StringBuilder();
        sb.append("var cutscene = DirectorsCamera.cutscene()");
        if (!recording.name().isEmpty()) {
            sb.append(".id(\"").append(recording.name()).append("\")");
        }
        sb.append('\n');
        sb.append("    .setDurationSeconds(").append(format(recording.effectiveDuration() / 20.0)).append(")\n");
        sb.append("    .setCurve(\"").append(recording.curve().name()).append("\")\n");
        sb.append("    .setTimeEasing(\"").append(recording.timeEasing().name()).append("\")\n");
        sb.append("    .setLookEasing(\"").append(recording.lookEasing().name()).append("\")");
        if (recording.isAnchored()) {
            sb.append("\n    .anchored(\"").append(recording.anchor()).append("\")");
        }
        sb.append(";\n");
        sb.append("cutscene.getPath()").append(pointLines(recording.keyframes())).append(";\n");
        return sb.toString();
    }

    public static String pointLines(List<CameraPos> keyframes) {
        StringBuilder sb = new StringBuilder();
        for (CameraPos pos : keyframes) {
            sb.append("\n    ").append(pointLine(pos));
        }
        return sb.toString();
    }

    public static String pointLine(CameraPos pos) {
        return ".addPoint(" + format(pos.pos().x) + ", " + format(pos.pos().y) + ", " + format(pos.pos().z) + ", "
            + format(pos.yaw()) + ", " + format(pos.pitch()) + ", " + format(pos.roll()) + ")";
    }

    public static String toJson(CameraRecording recording) {
        return CutsceneJsonWriter.pretty(recording.toDefinition());
    }

    public static String format(double value) {
        String s = String.format(Locale.ROOT, "%.2f", value);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "");
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s.equals("-0") ? "0" : s;
    }
}
