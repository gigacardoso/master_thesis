library(doSNOW)

predict <- function(states, exam, iter, steps){
library(HMM)
#print(paste(exam, "loading data"))
d <- read.csv(file=paste(c("C:\\ALSHMM\\data\\",steps,"\\nomissing\\approach1_",exam,"_",steps,".csv"),collapse=""),head=TRUE,sep=",", stringsAsFactors=FALSE)

#print(summary(d))
prob <- function (x) {x / sum (x)}  # Makes it a probability (it sums to 1)
vals <- getPossibleValues(exam)
numSymb <- length(vals)
#vals <- c(vals, "$")

rows <- nrow(d)
fold <- floor(rows/10)

folds <- c(1,fold,fold*2,fold*3,fold*4,fold*5,fold*6,fold*7,fold*8,fold*9)
fileConn<-file(paste(c("C:\\PROACT_2013_08_27_ALL_FORMS\\hmm\\data\\",exam,"_Predictions.csv"),collapse=""))

for(p in 1:9){
	#print(p)
	if( p == 1){
	train <- d[folds[p+1]+1:nrow(d)-fold,]
	test <- d[folds[p]:folds[p+1],]
	#print(paste("train ", folds[p+1]+1,"-" , nrow(d)))
	#print(paste("test ", folds[p],"-" , folds[p+1]))
	}else{
		if(p == 9){
			train <- d[1:folds[p]-1,]
			test <- d[folds[p]:nrow(d),]
			#print(paste("train ", 1,"-",folds[p]-1))
			#print(paste("test ", folds[p],"-" , nrow(d)))
		}else {
			train1 <- d[1:folds[p]-1,]
			train2 <- d[folds[p+1]+1:nrow(d),]
			size <- nrow(d) - folds[p+1]
			test <- d[folds[p]:folds[p+1],]
			train <- rbind(train1, train2[1,])
			for(k in 2:size){
				train <- rbind(train, train2[k,])
			}
			#print(paste("train ", 1,"-",folds[p]-1))
			#print(paste("test ", folds[p],"-" , folds[p+1]))
			#print(paste("train ", folds[p+1]+1,"-",nrow(d)))
		}
	}
	
	stat <- c("a","b")
	for(i in 1:(states-2)){
		stat <- c( stat , paste("s",i))
	}
	
	#print(paste(exam, "initialization"))
	

	hmms <- vector()
	for(symb in 1:numSymb){
		s <- vals[symb]
		#print(s)
		hmm = initHMM(stat, vals, startProbs=(prob (runif (states))),
			transProbs=apply (matrix (runif(states*states), states), 1, prob),
			emissionProbs=apply (matrix (runif(states*length(vals)), states), 1, prob))	

		##print(hmm)
		#train hmm
		#print(paste(exam, "Build training", s))
		m = 1
		
		observations <- vector()
		for (i in 1:nrow(train)) {
			if(train[i,ncol(train)] == s){
				for (j in 2:ncol(train)) {
					observations[m] <- train[[i,j]]
					m = m + 1
				}
				#observations[m] <- "$"
				#m = m + 1
			}
		}
		#print(paste(exam, "BaumWelch", "iter ->", iter))
		#print(observations)
		#print(length(observations))
		if(length(observations) > 52){
		vt = baumWelch(hmm, observations, maxIterations=iter, delta=1E-9, pseudoCount=0)
		}else {
			vt <- list(hmm = "11", difference = "as")
			hmm = initHMM(stat, vals, startProbs=(prob (runif (states))),
			transProbs=matrix(0, states, states),
			emissionProbs= matrix(0, states, length(vals)))
			vt$hmm <- hmm
		}
		hmms <- c(hmms,vt)
		#print("yo")
		#print(vt$hmm)
	}
	##print(vt$hmm)
	#predict
	values <- getPossibleValues(exam)
	
	#print(paste(exam, "Forward"))
	for (i in 1:nrow(test)) {
		m = 1
		observations <- vector()
		#get values of row
		for (j in seq(2, ncol(test)-1, by=1)) {
			observations[m] <- test[[i,j]]
			m = m + 1
		}
		#forward and save for every possible value
		probs <- vector()
		index<-1
		for(j in 1:length(values)){
			observations[m] <- values[j]
			#observations[(m+1)] <- "$"
			##print(j)
			##print(observations)
			##print(hmms[index]$hmm)
			f <- forward(hmms[index]$hmm, observations)
			##print(observations)
			##print(f)
			probs[j] <- f[1,ncol(f)]
			for(k in 2:states){
				if (f[k,ncol(f)] > probs[j]){
					probs[j] <- f[k,ncol(f)]
				}
			}
			index <- index+2
		}
		max <- (-2000000)
		for(j in 1:length(values)){
			if (probs[j] > max){
				index <- j
				max <- probs[j]
			}
		}
		#if( index != 2){
	#		#print(observations)
	#		#print(paste("chosen",vals[index]))
	#	}
		obs <- vector()
		obs[1] <- test[i,1]
		obs[2] <- values[index]
		##print(test[i,])
		##print(obs)
		if( p == 1 && i == 1){
			text <- paste(obs,collapse=",")
		}else{
			text <- c(text,paste(obs,collapse=","))
		}
	}
}
write(text,fileConn)
close(fileConn)
#print(paste(exam, "<-----------------------------------   DONE"))
}

getPossibleValues <- function(exam){
	if(exam == "Demo1"){
		vals <- c("Female","Male")
	}else{
		vals <- c("B0","B1","B2","B3","B4","B5")
	}
	#print(vals)
	return(vals)
}

#
#				----------- RUN ------------------------
#


#"Demo1","Demo2","Demo3","SVC2","SVC5","SVC6","SVC7","Vitals2","Vitals3","Vitals6","Vitals7","Vitals8","Vitals9"
exams <- c("Demo1","Demo2","Demo3","SVC2","SVC5","SVC6","SVC7","Vitals2","Vitals3","Vitals6","Vitals7","Vitals8","Vitals9")
#cl <- makeCluster(3, type="SOCK")
#registerDoSNOW(cl)

#writeLines(c(""), "C:\\PROACT_2013_08_27_ALL_FORMS\\hmm\\__log.txt")
#sink("C:\\PROACT_2013_08_27_ALL_FORMS\\hmm\\__log.txt", append=TRUE)
tates <- 6
ter <- 1
teps <- 6
print("--------------------------------------UNI----------------------------------------")
print(paste("states: ",tates,"iter: ",ter, "steps: ",teps))

#foreach(i = 1:length(exams) , .combine=rbind) %dopar% {
for(i in 1:length(exams)){
	#sink("C:\\PROACT_2013_08_27_ALL_FORMS\\hmm\\__log.txt", append=TRUE)
	#print(exams)
	#predict(#states,exam, #iter, #steps)
	
	# Start the clock!
	ptm <- proc.time()
	#print(exams[i])
	predict(tates,exams[i], ter, teps)
	
	timed <- (proc.time() - ptm)
	print(exams[i])
	print(timed)
	#sink()
}
#sink()
#stopCluster(cl)
