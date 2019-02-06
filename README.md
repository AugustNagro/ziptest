## Some benchmarks that prove parallel IO is a bad idea.

The benchmarks compare the single-shot time to unzip a ffmpeg distribution.
7Zip is required on Windows, and `unzip` on Mac/Linux.

```
Benchmark                Mode  Cnt     Score    Error  Units
DelegateToProcess.unzip    ss   10  1689.948 ± 54.177  ms/op
FixedThreadPool.unzip      ss   10  1090.838 ± 31.779  ms/op
ParallelStream.unzip       ss   10  1146.209 ± 39.790  ms/op
SingleThreaded.unzip       ss   10  1495.333 ± 42.212  ms/op
```

Using parallelism is ~37% faster (402ms) than single threaded.
This improvement is dubious; single threaded should be preferred.
Since my Mac Pro's 2015 SSD is serial (PCIe https://support.apple.com/kb/SP715?locale=en_US),
there is no real theoretical benefit for parallelism.