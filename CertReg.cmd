keytool -exportcert -keystore SKMChatServer.ks -storepass SKMChatServer -file SKM.crt -alias SKMChatServer
keytool -import -trustcacerts -alias skmroot -file .\SKM.crt -storepass changeit
keytool -import -trustcacerts -alias skmca -file .\SKM.crt -storepass changeit