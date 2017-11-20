package uff.ic.swlab.dataset_ertd.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private SWLabHost host;

    private String rawDataRootDir;
    private String htmlRootDir;
    private String rdfRootDir;

    //private String datasetVersion;
    private String ontologyVersion;
    private String fusekiDataset;
    private String datasetname;
    private String ontologyname;

    private String localOntologyHomepageName;
    private String localOntologyHomepageName2;
    private String localOntologyname;
    private String localDatasetHomepageName;
    private String localXMLDumpName;
    private String localTurtleDumpName;
    private String localJsonldDumpName;
    private String localNtriplesDumpName;

    private static String usename;
    private static String password;

    private String remoteOntologyHomepageName;
    private String remoteOntologyHomepageName2;
    private String remoteOntologyName;
    private String remoteDatasetHomepageName;
    private String remoteNtriplesDumpName;

    private String fusekiDataUrl;

    private Config() {
        try (InputStream input = new FileInputStream("./conf/host.properties");) {
            Properties prop = new Properties();
            prop.load(input);

            String hostname = prop.getProperty("hostname", "localhost");
            int httpPort = Integer.parseInt(prop.getProperty("httpPort", "8080"));
            int ftpPort = Integer.parseInt(prop.getProperty("ftpPort", "2121"));
            host = new SWLabHost(hostname, httpPort, ftpPort);

            usename = prop.getProperty("username");
            password = prop.getProperty("password");

        } catch (Throwable t) {
            usename = "";
            password = "";
            host = new SWLabHost("localhost", 8080, 2121);
        }

        //rawDataRootDir = "./data/" + datasetVersion + "/raw";
        //htmlRootDir = "./data/" + datasetVersion + "/html";
        //rdfRootDir = "./data/" + datasetVersion + "/rdf";
        rawDataRootDir = "./data/raw";
        htmlRootDir = "./data";
        rdfRootDir = "./data";

        ontologyVersion = "v1";
        ontologyname = fusekiDataset + "_" + ontologyVersion;

        //datasetVersion = "v3";
        fusekiDataset = "EntityRelatednessTestData";
        //datasetname = fusekiDataset + "_" + datasetVersion;
        datasetname = fusekiDataset;

        localOntologyHomepageName = htmlRootDir + "/ontology/" + datasetname + "/index.jsp";
        localOntologyHomepageName2 = htmlRootDir + "/ontology/" + ontologyname + "/index.jsp";
        localOntologyname = rdfRootDir + "/ontology/" + ontologyname + ".rdf";
        localDatasetHomepageName = htmlRootDir + "/dataset/" + fusekiDataset + "/index.jsp";
        localNtriplesDumpName = rdfRootDir + "/dataset/" + datasetname + ".nt.gz";

        remoteOntologyHomepageName = "/tomcat/ontology/" + datasetname + "/index.jsp";
        remoteOntologyHomepageName2 = "/tomcat/ontology/" + ontologyname + "/index.jsp";
        remoteOntologyName = "/tomcat/ontology/" + ontologyname + ".rdf";
        remoteDatasetHomepageName = "/tomcat/dataset/" + datasetname + "/index.jsp";
        remoteNtriplesDumpName = "/tomcat/dataset/" + datasetname + ".nt.gz";

        fusekiDataUrl = host.getDataURL(fusekiDataset);
    }

    private static Config config;

    public static Config getInsatnce() {
        if (config == null)
            config = new Config();
        return config;
    }

    public String rawDataRootDir() {
        return rawDataRootDir;
    }

    public String rdfRootDir() {
        return rdfRootDir;
    }

    public String fusekiDataset() {
        return fusekiDataset;
    }

    public String datasetname() {
        return datasetname;
    }

    public String ontologyname() {
        return ontologyname;
    }

    public String localOntologyHomepageName() {
        return localOntologyHomepageName;
    }

    public String localOntologyname() {
        return localOntologyname;
    }

    public String localDatasetHomepageName() {
        return localDatasetHomepageName;
    }

    public String localXMLDumpName() {
        return localXMLDumpName;
    }

    public String localTurtleDumpName() {
        return localTurtleDumpName;
    }

    public String localJsonldDumpName() {
        return localJsonldDumpName;
    }

    public String localNtriplesDumpName() {
        return localNtriplesDumpName;
    }

    public static String username() {
        return usename;
    }

    public static String password() {
        return password;
    }

    public String remoteOntologyHomepageName() {
        return remoteOntologyHomepageName;
    }

    public String remoteOntologyName() {
        return remoteOntologyName;
    }

    public String remoteDatasetHomepageName() {
        return remoteDatasetHomepageName;
    }

    public String remoteNtriplesDumpName() {
        return remoteNtriplesDumpName;
    }

    public String fusekiDataUrl() {
        return fusekiDataUrl;
    }

    public SWLabHost host() {
        return host;
    }
}
