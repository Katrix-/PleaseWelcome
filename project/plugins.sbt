logLevel := Level.Warn
addSbtPlugin("net.katsstuff" % "sbt-spongyinfo" % "1.0")

resolvers += Resolver.url("katrix-sbtplugins", url("https://dl.bintray.com/katrix/sbt-plugins"))(Resolver.ivyStylePatterns)
