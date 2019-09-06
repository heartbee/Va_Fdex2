这份代码将Fdex2脱壳代码集成到VA中，在Android7.0上运行成功。
原理：
（1）使用了epic（https://github.com/tiann/epic）添加到VA中做内部hook
（2）hook的函数是com.android.dex.Dex.getBytes().
（3）注入点为VA中的VClientImpl类中的bindApplicationNoCheck()函数中，在生成了Application后添加hook代码即可
