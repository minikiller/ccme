# CCFEMEORDERMATCH

### send 35=d
```
8=FIX5SP2｜35=d｜34=100｜52=20201230 23:00:06.529｜001=20201231 07:00:08.214｜5799=0｜911=877｜980=A｜779=20201227-17:08:57.866｜1682=18｜1180=430｜1300=74｜462=5｜207=88｜1151=BS｜6937=FHAR｜55=FHARG1-FHARM1｜48=333561｜22=8｜167=FUT｜461=FMIXSX｜15=MYR｜762=SP｜9779=N｜1142=F｜562=1｜1140=500｜969=0.02｜9787=1｜996=MYR｜1150=0｜731=1｜1143=0.2｜5796=18617｜864=2｜865=5｜1145=20210103-22:30:00.00｜865=7｜1145=20210226-09:15:00.00｜1141=2｜1022=GBX｜264=5｜1022=GBI｜264=2｜870=1｜871=1｜872=787457｜1234=0｜555=2｜602=332305｜603=8｜624=1｜623=1｜602=262853｜603=8｜624=2｜623=1｜
```

### send 35=X
```
8=FIX5SP2｜35=X｜34=2522073｜52=20201231 00:15:37.576｜001=20201231 08:15:37.678｜60=20201231-00:15:37.576｜5799=4｜268=1｜270=-2.5｜271=8｜48=143484｜83=99177｜346=6｜1023=1｜279=1｜269=0｜37705=0｜
```

### 1.login to bibiweiqi.com
### 2.tmux command
```
tmux a -t sunlf
```
### 3.start server
```
sudo java -jar ordermatch/target/quickfixj-examples-ordermatch-2.2.0-standalone.jar 
```
### 4. update server
```
sudo git pull
sudo mvn clean package
```
### 5.logout tmux
```
ctrl+B then press d
```

### 6. run
``` 
sudo java -cp marketdata/target/ccme-marketdata-2.2.0-standalone.jar quickfix.examples.executor.MarketdataServer
```

### run md


      普通单       隐含单
单脚  单脚普通单   单脚隐含单
双脚  双脚普通单   双脚隐含单

### get java jar process
```
 ps -ef | grep MarkerDataServer | awk '{print $2}'
```

### 7. example docker run MD|ME
```
生产使用时 openjdk:11.0.9.1 做基础image

sudo docker run --name 11.0.9.1 -d \
    -v /opt/project/ccme/marketdata/target/ccme-marketdata-2.2.0-standalone.jar:/home/feme-md.jar \
    openjdk:11 \
    nohup java -jar /home/feme-md.jar
```

### 8. use travis 
create token on github first,then run following command:
``` 
travis login --github-token 5c05edc78fcb7960a2abbb01aabb64a254bc3533 --com
travis encrypt-file deploy_rsa --add --com  
```

quickfix.examples.ordermatch.MarketClientApplication.updateMarketData(MarketClientApplication.java:224)