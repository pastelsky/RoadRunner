
def smoothcurve(input, alpha):
	output = []
	output.append(input[0])
	for i in xrange(1,len(input)):
		output.append( alpha * input[i] + (1 - alpha ) * output[i-1] )
	return output