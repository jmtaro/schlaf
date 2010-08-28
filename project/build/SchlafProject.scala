package sbt

import java.io.File
import sbt.Process._

//original: http://github.com/Yasushi/sbt-appengine-plugin

class SchlafProject(info: ProjectInfo) extends DefaultWebProject(info) {

  val scalatest = "org.scalatest" % "scalatest" % "1.2"
  val servlet = "javax.servlet" % "servlet-api" % "2.5"

  override def unmanagedClasspath =
    super.unmanagedClasspath +++ gae.classpath +++ gae.testpath

  lazy val devAppserverStart = DevAppserverTask.start
  lazy val devAppserverStop = DevAppserverTask.stop

  object DevAppserverTask {
    private lazy val server = new DevAppserverRun

    def start = task{
      args => task{ server.start(args) } dependsOn prepareWebapp
    }
    def stop = task{ server.stop }
  }

  object gae {
    def classpath: PathFinder = lib.user.api
    def testpath: PathFinder = lib.testing +++ lib.impl.apiStubs +++ lib.impl.apiLabs

    object sdk {
      def version =
        (lib.user.dir * "appengine-api-1.0-sdk-*.jar").get.toList match {
          case jar::_ =>
            val reg = """.*/appengine-api-1.0-sdk-(.+)\.jar""".r
            jar.absolutePath match {
              case reg(ver) => ver
              case _ => error("invalid jar file :" + jar)
            }
          case _ => error("appengine-api-*.jar not found")
        }
      def path =
        System.getenv("APPENGINE_SDK_HOME") match {
          case null => error("APPENGINE_SDK_HOME required")
          case sdk => Path fromFile new File(sdk)
        }
    }

    object lib {
      def dir = sdk.path / "lib"
      def agent = dir / "agent" / "appengine-agent.jar"
      def tools = dir / "appengine-tools-api.jar"
      def testing = dir / "testing" / "appengine-testing.jar"

      object user {
        def dir = lib.dir / "user"
        def api = dir / ("appengine-api-1.0-sdk-" + sdk.version + ".jar")
      }

      object impl {
        def dir = lib.dir / "impl"
        def apiStubs = dir / "appengine-api-stubs.jar"
        def apiLabs = dir / "appengine-api-labs.jar"
      }
    }

  }

  lazy val javaHome = new java.io.File(System.getProperty("java.home"))
  lazy val javaCmd = (Path.fromFile(javaHome) / "bin" / "java").absolutePath

  class DevAppserverRun() extends Runnable with ExitHook {
    ExitHooks.register(this)
    def name = "dev_appserver-shitdown"
    def runBeforeExiting() { stop() }

    val jvmOptions =
      List("-ea", "-javaagent:" + gae.lib.agent.absolutePath,
           "-cp", gae.lib.tools.absolutePath)

    private var running: Option[Process] = None

    def run() {
      running.foreach(_.exitValue())
      running = None
    }

    def start(args: Seq[String]): Option[String] = {
      if (running.isDefined){
        Some("dev_appserver already running")
      } else {
        val builder: ProcessBuilder =
          Process(javaCmd :: jvmOptions :::
                  "com.google.appengine.tools.development.DevAppServerMain" ::
                  args.toList ::: temporaryWarPath.relativePath :: Nil)
        running = Some(builder.run)
        new Thread(this).start()
        None
      }
    }

    def stop(): Option[String] = {
      running.foreach(_.destroy)
      running = None
      log.debug("stop")
      None//no error occurred
    }

  }

}

