## Contributing
### Contribution Rules
1. No SNAPSHOT dependencies on "develop" and obviously "master" branches

### Releasing
1. From the develop branch, prepare a release (example using a HTTP proxy):
<pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=80 jgitflow:release-start
</code></pre>
1. Update the CHANGELOG according to keepachangelog.com.
1. To perform the release (example using a HTTP proxy):
<pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=80 jgitflow:release-finish
</code></pre>
    If, after deployment, the command does not succeed because of some issue with the branches. Fix the issue, then re-run the same command but with 'noDeploy' option set to true to avoid re-deployment:
<pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=80 -DnoDeploy=true jgitflow:release-finish
</code></pre>
    If the command fails because of a gpg error such as "no gpg-agent available to this session" or "no pinentry", make sure you have installed a pinentry program (e.g. with package `pinentry-gnome3`), and that a gpg-agent is running with this pinentry program, for example:
 <pre><code>
    $ gpg-agent --daemon --pinentry-program /usr/bin/pinentry-gnome3
</code></pre>
    Then re-run the mvn command as above.
1. Connect and log in to the OSS Nexus Repository Manager: https://oss.sonatype.org/
1. Go to Staging Profiles and select the pending repository authzforce-*... you just uploaded with `jgitflow:release-finish`
1. Click the Release button to release to Maven Central.

More info on jgitflow: http://jgitflow.bitbucket.org/
