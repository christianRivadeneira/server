package controller.system;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import api.sys.model.SysCfg;
import service.MySQL.MySQLCommon;

public class AsadminController {

    private static String getString(InputStream is) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            is.close();
        }
        return sb.toString();
    }

    public static String domainRestart() throws Exception {
        Connection con = null;
        try {
            con = MySQLCommon.getConnection("sigmads", null);
            String gPath = SysCfg.select(con).glassPath;
            Process run = Runtime.getRuntime().exec(String.format("cmd /c \"" + gPath + "\\asadmin\" restart-domain"));
            run.waitFor();
            if (run.exitValue() != 0) {
                throw new Exception("El proceso terminó con código " + run.exitValue() + "\n" + getString(run.getErrorStream()));
            } else {
                return getString(run.getInputStream());
            }
        } finally {
            MySQLCommon.closeConnection(con);
        }
    }

    public static String undeploy(String appName, int port) throws Exception {
        Connection con = null;
        try {
            con = MySQLCommon.getConnection("sigmads", null);
            String gPath = SysCfg.select(con).glassPath;
            Process run = Runtime.getRuntime().exec(String.format("cmd /c \"%s\\asadmin\" --port=%d undeploy %s", gPath, port, appName));
            run.waitFor();
            if (run.exitValue() != 0) {
                throw new Exception("El proceso terminó con código " + run.exitValue() + "\n" + getString(run.getErrorStream()));
            } else {
                return getString(run.getInputStream());
            }
        } finally {
            MySQLCommon.closeConnection(con);
        }
    }

    public static String listApplications(int port) throws Exception {
        Connection con = null;
        try {
            con = MySQLCommon.getConnection("sigmads", null);
            String gPath = SysCfg.select(con).glassPath;
            Process run = Runtime.getRuntime().exec(String.format("cmd /c \"%s\\asadmin\" --port=%d list-applications ", gPath, port));
            run.waitFor();
            if (run.exitValue() != 0) {
                throw new Exception("El proceso terminó con código " + run.exitValue() + "\n" + getString(run.getErrorStream()));
            } else {
                return getString(run.getInputStream());
            }
        } finally {
            MySQLCommon.closeConnection(con);
        }
    }

    /* public static String deploy(CompressedFile back, String appName, int port) throws Exception {
        Connection con = null;
        try {
            con = MySQLCommon.getConnection("sigmads", null);
            String gPath = SysCfg.select(con).glassPath;
            File tmp = File.createTempFile("app", ".war");
            try (FileOutputStream fos = new FileOutputStream(tmp); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                bos.write(back.getBdata());
            }
            Process run = Runtime.getRuntime().exec(String.format("cmd /c \"%s\\asadmin\" --port=%d deploy --contextroot=%s --name=%s --force  %s", gPath, port, appName, appName, tmp.getAbsolutePath()));
            run.waitFor();
            if (run.exitValue() != 0) {
                throw new Exception("El proceso terminó con código " + run.exitValue() + "\n" + getString(run.getErrorStream()));
            } else {
                return getString(run.getInputStream());
            }
        } finally {
            MySQLCommon.closeConnection(con);
        }
    }*/
}
