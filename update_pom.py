import os
import sys
import string
import re
import getopt

aarPattern = re.compile(r'^([a-zA-Z_-]+)-library-release-v([0-9.]+).aar')

def parseAar(aar, groupId) :
    m = aarPattern.match(aar)
    if m is None :
        return None
    gs = m.groups()
    if gs is None :
        return gs

    artifactId = gs[0]
    version = gs[1]
    flavor = ""
    if artifactId.find("-") >= 0 :
        splits = artifactId.split("-")
        flavor = splits[len(splits) - 1]

    return {
        "groupId" : groupId,
        "artifactId" : artifactId,
        "version" : version,
        "flavor" : flavor,
    }

def parseCompile(line, prefix, groupId) :
    res = [re.compile('^\s*compile\s*\'\(*([a-zA-Z0-9._-]+):([a-zA-Z0-9_-]+):([a-zA-Z0-9._-]+)')]
    if prefix is not None and prefix != "" :
        res.append(re.compile('^\s*' + prefix + 'Compile\s*\'\(*([a-zA-Z0-9._-]+):([a-zA-Z0-9_-]+):([a-zA-Z0-9._-]+)'))
    
    gs = None
    for rep in res :
        m = rep.match(line)
        if m is not None and m.groups() is not None :
            gs = m.groups()
            break

    if gs is None :
        return gs

    return {
        "groupId" : gs[0],
        "artifactId" : gs[1],
        "version" : gs[2],
    }

def parseGradleDependencies(gradleFile, prefix, groupId) :
    lines = open(gradleFile).readlines()
    dependencies = []
    inDependency = False
    for line in lines :
        if line.find("dependency begin") >= 0 :
            inDependency = True
        if line.find("dependency end") >= 0 :
            inDependency = False
        c = parseCompile(line, prefix, groupId)
        # if c is None or not c.has_key("groupId") or c["groupId"] != groupId or c["artifactId"] == "ads" :
        if c is None or not c.has_key("groupId") or not inDependency:
            continue

        dependencies.append(c)

    return dependencies

pomTmpl = string.Template('''<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://maven.apache.org/POM/4.0.0"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>${groupId}</groupId>
    <artifactId>${artifactId}</artifactId>
    <version>${version}</version>
    <packaging>aar</packaging>

    <dependencies>
        ${dependencies}
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>com.simpligility.maven.plugins</groupId>
                <artifactId>android-maven-plugin</artifactId>
                <version>4.1.0</version>
                <extensions>true</extensions>         
                <configuration>
                    <sign>
                        <debug>false</debug>
                    </sign>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>''')

dependencyTmpl = string.Template('''
        <dependency>
            <groupId>${groupId}</groupId>
            <artifactId>${artifactId}</artifactId>
            <version>${version}</version>
        </dependency>
''')

mavenTmpl = string.Template('''${maven} deploy:deploy-file -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} -Dpackaging=aar -Dfile=${file} -DpomFile=${pomFile} -DrepositoryId=${repositoryId} -Durl=${url}''')

def makePom(pomTmpl, dependencyTmpl, mainMeta, dependentMetas) :
    dependencies = "".join([dependencyTmpl.substitute(**m) for m in dependentMetas])
    mainMeta["dependencies"] = dependencies
    pom = pomTmpl.substitute(**mainMeta)
    return pom

if __name__ == '__main__':

    aarDir = None
    gradle = None
    snapshot = False
    output = "."

    opts, args = getopt.getopt(sys.argv[1:], 'h', ['aarDir=', 'gradle=', 'snapshot=', 'maven=', 'output='])
    for o, a in opts:
        if o in ('--aarDir') :
            aarDir = a
        elif o in ('--gradle') :
            gradle = a
        elif o in ('--snapshot') :
            snapshot = bool(a)
        elif o in ('--maven') :
            maven = a
        elif o in ('--output') :
            output = a
        elif o in ('-h') :
            print "--aarDir --gradle --snapshot --maven --output"
            exit()

    if aarDir is None :
        print "aarDir needed!"
        exit(1)
    
    if gradle is None :
        print "gradle needed!"
        exit(1)

    mavenCmds = []

    for f in os.listdir(aarDir) :
        if not f.endswith(".aar") :
            continue

        metaAar = parseAar(f, "com.solid")
        if metaAar is None :
            continue

        if snapshot :
            metaAar["version"] = metaAar["version"] + "-SNAPSHOT"

        flavor = metaAar["flavor"]

        metaDependencies = parseGradleDependencies(gradle, flavor, "com.solid")

        pomFile = "pom"
        if flavor is not None and flavor != "" :
            pomFile = pomFile + "_" + flavor
        if snapshot :
            pomFile = pomFile + "-SNAPSHOT"
        pomFile = pomFile + ".xml"

        open(output + os.path.sep + pomFile, "w").write(makePom(pomTmpl, dependencyTmpl, metaAar, metaDependencies))

        mavenCmds.append(mavenTmpl.substitute(**{
            "maven" : maven,
            "groupId" : metaAar["groupId"],
            "artifactId" : metaAar["artifactId"],
            "version" : metaAar["version"],
            "file" : f,
            "pomFile" : pomFile,
            "repositoryId" : "$1",
            "url" : "$2"
        }))

    mavenUploadSh = "maven_upload"
    if snapshot :
        mavenUploadSh = mavenUploadSh + "-SNAPSHOT"
    mavenUploadSh = mavenUploadSh + ".sh"
    open(output + os.path.sep + mavenUploadSh, "w").write("\n".join(mavenCmds))