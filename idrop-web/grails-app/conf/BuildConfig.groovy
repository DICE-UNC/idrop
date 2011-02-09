grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
       excludes 'ehcache'
    }
    log "warn" // LOG level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenLocal()
        mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
		grailsPlugins()
		grailsHome()
		grailsCentral()
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
		test 'org.irods:jargon-test:2.4.1-SNAPSHOT'
		compile 'org.irods:jargon-core:2.4.1-SNAPSHOT'
		compile 'org.irods:jargon-security:2.4.1-SNAPSHOT'
		compile 'org.springframework.security:spring-security-core:3.0.5.RELEASE'
		compile 'org.springframework.security:spring-security-web:3.0.5.RELEASE'
		compile 'org.springframework.security:spring-security-config:3.0.5.RELEASE'
		
		compile( group: 'log4j', name: 'log4j', version: '1.2.16', export: false )
		//compile 'log4j:log4j:1.2.16'
        // runtime 'mysql:mysql-connector-java:5.1.5'
    }
}